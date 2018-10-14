package org.kissfarm.agent.lifecycle;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.kissfarm.agent.client.api.ControllerConnection;
import org.kissfarm.agent.client.websocket.StompFrameHandlerPayloadTypeAware;
import org.kissfarm.agent.client.websocket.StompSessionEvt;
import org.kissfarm.agent.client.websocket.StompSessionHolder;
import org.kissfarm.agent.config.FarmConfigState;
import org.kissfarm.agent.config.NodeConfig;
import org.kissfarm.agent.config.NodeConfigHolder;
import org.kissfarm.agent.node_identity.api.NodeIdentityHolder;
import org.kissfarm.shared.api.AppDefFolderReader;
import org.kissfarm.shared.api.Compressor;
import org.kissfarm.shared.config.dto.AppDefConfig;
import org.kissfarm.shared.impl.AppDefFolderReaderImpl;
import org.kissfarm.shared.websocket.WebSocketCommons;
import org.kissfarm.shared.websocket.api.NodeConfigRequest;
import org.kissfarm.shared.websocket.api.NodeConfigResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompSession.Subscription;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import com.google.gson.Gson;

public class InitConfigLifecycle extends LifecycleSyncBase implements NodeConfigHolder {
	private Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	private NormalLifecycle normalLifecycle;
	@Autowired
	private RegistrationLifecycle registrationLifecycle;
	@Autowired
	private StompSessionHolder stompSessionHolder;
	@Autowired
	private NodeIdentityHolder nodeIdentityHolder;
	@Autowired
	private ControllerConnection controllerConnection;
	@Autowired
	private ScheduledExecutorService executorService;
	@Autowired
	private Compressor compressor;

	private String packageLocation;
	private File farmConfigStateFile;
	private int nodeConfigRequestTimeoutSeconds = 60;

	protected Gson gson = new Gson();
	private AppDefFolderReader appDefFolderReader = new AppDefFolderReaderImpl();

	private StompSessionEvt session;
	private Subscription subscription;
	private CompletableFuture<Lifecycle> nextLifeCycle = null;
	private NodeConfig nodeConfig;
	private ScheduledFuture<?> nodeConfigRequestSchedule;

	@Override
	protected Lifecycle doStep() throws Exception {
		if (doWeHaveConfig()) {
			log.debug("We already have configuration, switching to next lifecycle. Version: {}",
					nodeConfig.getVersion());
			return normalLifecycle;
		}

		log.debug("No nodeConfig found locally. Will have to request one from controller");

		nextLifeCycle = new CompletableFuture<>();

		session = stompSessionHolder.getSession();
		if (!session.isConnected()) {
			log.debug("Lost connection. Falling back");
			return registrationLifecycle;
		}
		session.addConnectionLostListener(onConnectionLost);

		// Let Controller know we need config
		log.debug("Requesting config from server");
		String nodeId = nodeIdentityHolder.getNodeIdentity().getId();
		subscription = session.getStompSession().subscribe(WebSocketCommons.getServerToNodeTopic(nodeId), onMessage);
		nodeConfigRequestSchedule = executorService.schedule(nodeConfigRequestWatcher, nodeConfigRequestTimeoutSeconds,
				TimeUnit.SECONDS);
		log.debug("nodeConfigRequestWatcher armed");

		session.send(WebSocketCommons.getNodeToServerDestination(), new NodeConfigRequest(nodeId, null));
		log.debug("Requested Node Config from controller. Waiting...");

		try {
			log.debug("Block and wait for next lifecycle step");
			return nextLifeCycle.get();
		} catch (InterruptedException ie) {
			log.debug("had InterruptedException, going to close agent", ie);
			return null;
		} finally {
			safeUnsubscribe();
		}
	}

	private boolean doWeHaveConfig() throws IOException {
		try {
			if (!farmConfigStateFile.exists()) {
				return false;
			}

			String farmConfigStateJson = FileUtils.readFileToString(farmConfigStateFile, "UTF-8");
			FarmConfigState farmConfigState = gson.fromJson(farmConfigStateJson, FarmConfigState.class);
			if (farmConfigState.getActiveVersion() == null) {
				return false;
			}

			initNodeConfig(farmConfigState.getActiveVersion());
			return true;
		} catch (Throwable t) {
			log.warn("Failed to check if we have config", t);
			return false;
		}
	}

	private void initNodeConfig(String activeVersion) {
		try {
			nodeConfig = new NodeConfig();
			nodeConfig.setVersion(activeVersion);
			File configBasePath = buildConfigFolderName(activeVersion);
			Preconditions.checkState(configBasePath.exists(), "Node config folder doesnt exist: %s",
					configBasePath.getAbsolutePath());
			nodeConfig.setAppDefs(readAppDefinitions(configBasePath));
			nodeConfig.setConfigBasePath(configBasePath.getAbsolutePath());
		} catch (Throwable t) {
			throw new RuntimeException("Failed to load config from disk", t);
		}
	}

	protected Map<String, AppDefConfig> readAppDefinitions(File workDir) {
		// NOTE: Same logic located in FarmConfigReader. It would be nice to refactor it
		// and have in one place
		WildcardFileFilter dirFilter = new WildcardFileFilter(AppDefFolderReaderImpl.PREFIX_APP + "*");
		Iterator<File> iterator = FileUtils.iterateFilesAndDirs(workDir, DirectoryFileFilter.DIRECTORY, dirFilter);
		List<AppDefConfig> ret = new ArrayList<>();
		for (; iterator.hasNext();) {
			File appDir = iterator.next();
			if (!dirFilter.accept(appDir)) {
				continue;
			}

			AppDefConfig appDef = appDefFolderReader.readAppDefConfig(appDir);
			ret.add(appDef);
		}
		Preconditions.checkState(ret.size() > 0, "No applications found in the config folder: %s", workDir);
		return ret.stream().collect(Collectors.toMap(x -> x.getName(), x -> x));
	}

	private void safeUnsubscribe() {
		try {
			if (subscription != null) {
				subscription.unsubscribe();
				subscription = null;
			}
			if (session != null) {
				session.removeConnectionLostListener(onConnectionLost);
				session = null;
			}
		} catch (Throwable t) {
			log.debug("Failed to unsubscribe", t);
		}
	}

	private Consumer<StompSessionEvt> onConnectionLost = new Consumer<StompSessionEvt>() {
		@Override
		public void accept(StompSessionEvt t) {
			log.debug("Received notification about closed connection");
			safeUnsubscribe();
			nextLifeCycle.complete(registrationLifecycle);
		}
	};

	/**
	 * If engaged - it means time is out
	 */
	private Runnable nodeConfigRequestWatcher = new Runnable() {
		@Override
		public void run() {
			synchronized (nodeConfigRequestWatcher) {
				if (nextLifeCycle.isDone() || nodeConfigRequestSchedule == null) {
					log.debug(
							"It appears onNodeConfigResponse completed successfully. Disengaging nodeConfigRequestWatcher");
					return;
				}

				log.debug("nodeConfigRequestWatcher engaged");

				safeUnsubscribe();
				nodeConfigRequestSchedule = null;
				nextLifeCycle.complete(registrationLifecycle);
			}
		}
	};

	private StompFrameHandler onMessage = new StompFrameHandlerPayloadTypeAware() {
		@Subscribe
		public void onNodeConfigResponse(NodeConfigResponse dto) {
			try {
				synchronized (nodeConfigRequestWatcher) {
					if (nextLifeCycle.isDone() || nodeConfigRequestSchedule == null) {
						log.debug(
								"It appears nodeConfigRequestWatcher was enganged. Skipping processing of NodeConfigResponse");
						return;
					}

					log.debug("Got the link to FarmConfig package for us: {}", dto.getRelativeUrlPath());

					String targetFilename = packageLocation + File.separator + dto.getVersion() + "."
							+ compressor.getExtension();
					downloadPackage(dto, targetFilename);
					decompressPackage(dto, targetFilename);
					FarmConfigState farmConfigState = initFarmConfigState(dto);
					initNodeConfig(farmConfigState.getActiveVersion());

					safeUnsubscribe();
					nodeConfigRequestSchedule.cancel(false);
					nodeConfigRequestSchedule = null;
					log.debug("nodeConfigRequestWatcher disarmed");
					nextLifeCycle.complete(normalLifecycle);
				}
			} catch (Throwable t) {
				log.error("Failed to process NodeConfigResponse", t);
				// NOTE: We'll let it sit in this state. Because timeout watcher will eventually
				// engage and fallback to previous lifecycle. If there is a problem we'd
				// rather not hurry with retries
			}
		}

		private void downloadPackage(NodeConfigResponse dto, String targetFilename) throws IOException {
			File targetFile = new File(targetFilename);
			if (targetFile.exists()) {
				FileUtils.forceDelete(targetFile);
			}
			controllerConnection.downloadFile(dto.getRelativeUrlPath(), targetFilename);
		}

		private FarmConfigState initFarmConfigState(NodeConfigResponse dto) throws IOException {
			FarmConfigState farmConfigState = new FarmConfigState();
			farmConfigState.setActiveVersion(dto.getVersion());
			String farmConfigStateJson = gson.toJson(farmConfigState);
			FileUtils.write(farmConfigStateFile, farmConfigStateJson, "UTF-8");
			return farmConfigState;
		}

		private void decompressPackage(NodeConfigResponse dto, String archiveFilename) throws IOException {
			File targetFolder = buildConfigFolderName(dto.getVersion());
			if (targetFolder.exists()) {
				FileUtils.forceDelete(targetFolder);
			}

			compressor.decompress(new File(archiveFilename), targetFolder);
			// NOTE: We're assumming here we're using same compressor as Controller did.
			// Later on we might want to make a resolver
			Preconditions.checkArgument(archiveFilename.endsWith(compressor.getExtension()),
					"Archived file extension doesn't much compressor capability");

			// NOTE: git and tar will preserve file permissions so there is no need to set
			// it. It brings a little challenge in case user wants to commit file to git
			// repo using Windows OS, but hey, if we get to this point, we'll add support
			// for it

			FileUtils.deleteQuietly(new File(archiveFilename));
		}
	};

	private File buildConfigFolderName(String version) {
		return new File(packageLocation + File.separator + version);
	}

	public String getPackageLocation() {
		return packageLocation;
	}

	public void setPackageLocation(String packageLocation) {
		this.packageLocation = packageLocation;
		if (packageLocation == null) {
			return;
		}

		File packageLocationFile = new File(packageLocation);
		Preconditions.checkArgument(packageLocationFile.exists() || packageLocationFile.mkdirs(),
				"Invalid packageLocation %s", packageLocation);
	}

	public String getFarmConfigStateFile() {
		return farmConfigStateFile.getAbsolutePath();
	}

	public void setFarmConfigStateFile(String farmConfigStateFile) {
		this.farmConfigStateFile = new File(farmConfigStateFile);
	}

	public AppDefFolderReader getAppDefFolderReader() {
		return appDefFolderReader;
	}

	public void setAppDefFolderReader(AppDefFolderReader appDefFolderReader) {
		this.appDefFolderReader = appDefFolderReader;
	}

	@Override
	public NodeConfig getNodeConfig() {
		return nodeConfig;
	}

	public int getNodeConfigRequestTimeoutSeconds() {
		return nodeConfigRequestTimeoutSeconds;
	}

	public void setNodeConfigRequestTimeoutSeconds(int nodeConfigRequestTimeoutSeconds) {
		this.nodeConfigRequestTimeoutSeconds = nodeConfigRequestTimeoutSeconds;
	}

}
