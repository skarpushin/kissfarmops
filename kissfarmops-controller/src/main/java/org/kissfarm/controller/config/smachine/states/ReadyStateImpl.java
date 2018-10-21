package org.kissfarm.controller.config.smachine.states;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.kissfarm.controller.config.api.FarmConfigFolderReader;
import org.kissfarm.controller.config.api.FarmConfigPackager;
import org.kissfarm.controller.config.dto.FarmConfig;
import org.kissfarm.controller.config.dto.GitConfig;
import org.kissfarm.controller.config.impl.FarmConfigTools;
import org.kissfarm.controller.config.mvc.FarmConfigRestController;
import org.kissfarm.controller.config.smachine.dtos.FarmConfigMachineVariables;
import org.kissfarm.controller.config.smachine.dtos.PullConfigUpdateRequest;
import org.kissfarm.controller.services.app_instance.api.AppInstanceService;
import org.kissfarm.controller.services.app_instance.dto.AppInstanceRow;
import org.kissfarm.controller.services.nodes.api.NodeStatus;
import org.kissfarm.controller.services.nodes.api.NodeStatusService;
import org.kissfarm.controller.services.nodes.api.NodeTag;
import org.kissfarm.controller.services.nodes.api.NodeTagService;
import org.kissfarm.controller.websockets.api.StompOutboundGateway;
import org.kissfarm.shared.config.dto.AppDefConfig;
import org.kissfarm.shared.config.dto.NodeNowUsesConfigReport;
import org.kissfarm.shared.tools.IdTools;
import org.kissfarm.shared.websocket.api.NodeConfigRequest;
import org.kissfarm.shared.websocket.api.NodeConfigResponse;
import org.kissmachine.api.dto.SmStateData;
import org.kissmachine.api.machine.SmTransitionToState;
import org.kissmachine.api.machine.StateMachine;
import org.kissmachine.api.state.SmStateKind;
import org.kissmachine.impl.state.Void2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.summerb.approaches.jdbccrud.api.dto.PagerParams;
import org.summerb.approaches.jdbccrud.api.dto.PaginatedList;
import org.summerb.approaches.jdbccrud.api.exceptions.EntityNotFoundException;
import org.summerb.approaches.jdbccrud.api.query.Query;
import org.summerb.approaches.security.api.exceptions.NotAuthorizedException;

import com.google.common.base.Preconditions;

/**
 * This state is responsible for initial download of the config
 * 
 * @author Sergey Karpushin
 *
 */
public class ReadyStateImpl extends FarmConfigStateAbstract<GitConfig, Void2, Void2, FarmConfigMachineVariables> {
	public static final String NAME = "Ready";

	@Autowired
	private FarmConfigFolderReader farmConfigFolderReader;
	@Autowired
	private StompOutboundGateway stompOutboundGateway;
	@Autowired
	private FarmConfigPackager farmConfigPackager;
	@Autowired
	private NodeTagService nodeTagService;
	@Autowired
	private NodeStatusService nodeStatusService;
	@Autowired
	private AppInstanceService appInstanceService;

	private FarmConfig farmConfig;

	public ReadyStateImpl() {
		handleByPayload(PullConfigUpdateRequest.class, msg -> onPullRequest(msg));
		handleByPayload(GitConfig.class, msg -> onUpdateGitConfig(msg.getPayload()));
		// NOTE: NodeConfigRequest handle is a part of base class
		handleByPayload(NodeNowUsesConfigReport.class, msg -> onNodeNowUsesConfigReport(msg.getPayload()));
	}

	private SmTransitionToState onUpdateGitConfig(GitConfig newGitConfig) {
		GitConfig curGitConfig = vars().getGitConfig();
		if (curGitConfig.equals(newGitConfig)) {
			// no change, really
			log.info("Received GitConfig, but there is no change. Ignoring.");
			return null;
		}

		if (curGitConfig.getUri().equals(newGitConfig.getUri())
				&& curGitConfig.getBranch().equals(newGitConfig.getBranch())) {
			vars().setGitConfig(newGitConfig);
			// NOTE: We are "witching" state to ourselves here to a) indicate credentials
			// change in the log; b) save it to db
			return new SmTransitionToState("Repo credentials changed", false, ReadyStateImpl.NAME, null);
		}

		return new SmTransitionToState("Repo location changed. Need to clone it from new location", false,
				ChangingRepoStateImpl.NAME, newGitConfig);
	}

	@Override
	protected SmTransitionToState handleResumeStateAction(Message<SmStateData> message, StateMachine stateMachine) {
		this.stateMachine = stateMachine;
		setStateData(message.getPayload());

		// quick validate active config
		try {
			farmConfig = farmConfigFolderReader.readFarmConfig(new File(vars().getActiveWorkTree()),
					vars().getActiveVersion());
			Preconditions.checkState(farmConfig != null, "FarmConfig read failure");
			Preconditions.checkState(farmConfig.getAppDefs().size() > 0, "No apps found in config");
			return handleInitStateAction(message, stateMachine);
		} catch (Throwable t) {
			log.error("Failed to verify active FarmConfig. Will have to roll back to Clonning state", t);
			SmTransitionToState ret = new SmTransitionToState(
					"Failed to resume Ready state, no farm config found at " + vars().getActiveWorkTree()
							+ ". Will fallback to Clonning state",
					false, ClonningConfigStateImpl.NAME, vars().getGitConfig());
			vars().setActiveVersion(null);
			vars().setActiveWorkTree(null);
			return ret;
		}
	}

	@Override
	protected SmTransitionToState handleInitStateAction(Message<SmStateData> message, StateMachine stateMachine) {
		SmTransitionToState ret = super.handleInitStateAction(message, stateMachine);
		if (ret != null) {
			return ret;
		}

		farmConfig = farmConfigFolderReader.readFarmConfig(new File(vars().getActiveWorkTree()),
				vars().getActiveVersion());

		// TBD: There might be a temptation to see if there are nodes which are waiting
		// for the configuration and let them know it. But that will be redundant
		// because Nodes have timeout mechanism to re-request config if Controller
		// doesn't respond for a long time. InitConfigLifecycle

		return null;
	}

	private SmTransitionToState onPullRequest(Message<PullConfigUpdateRequest> msg) {
		return new SmTransitionToState("Received request to check for updates", false, PullingUpdatesStateImpl.NAME,
				null);
	}

	@Override
	protected SmTransitionToState onNodeConfigRequest(Message<NodeConfigRequest> msg) {
		super.onNodeConfigRequest(msg);
		NodeConfigRequest payload = msg.getPayload();
		try {
			Set<String> nodeTags = getNodesTagSet(payload.getNodeId());
			String packageFilename = farmConfigPackager.preparePackage(farmConfig, vars().getActiveWorkTree(),
					nodeTags);

			NodeConfigResponse response = new NodeConfigResponse();
			response.setRelativeUrlPath(FarmConfigRestController.getFarmConfigPackageUrlPath(packageFilename));
			response.setVersion(farmConfig.getVersion());

			stompOutboundGateway.sendToNode(payload.getNodeId(), response);
		} catch (Throwable t) {
			log.error("Failed to prepare package for node " + payload.getNodeId(), t);
		}
		return null;
	}

	private Set<String> getNodesTagSet(String nodeId) throws NotAuthorizedException {
		PaginatedList<NodeTag> tags = nodeTagService.query(PagerParams.ALL,
				Query.n().eq(NodeTag.FN_SUBJECT_ID, nodeId));
		Preconditions.checkArgument(tags.getTotalResults() > 0, "No tags found for the node " + nodeId);
		return tags.getItems().stream().map(x -> x.getTag()).collect(Collectors.toSet());
	}

	private SmTransitionToState onNodeNowUsesConfigReport(NodeNowUsesConfigReport payload) {
		try {
			NodeStatus nodeStatus = nodeStatusService.findById(payload.getNodeId());
			Preconditions.checkArgument(nodeStatus != null,
					"How is it possible. Node is reporting about config usage but it doesn't have a status record yet. Node %s",
					payload.getNodeId());
			if (payload.getVersion().equals(nodeStatus.getVersion())) {
				return null;
			}

			// Update node status
			nodeStatus.setVersion(payload.getVersion());
			nodeStatusService.update(nodeStatus);

			if (!farmConfig.getVersion().equals(payload.getVersion())) {
				log.warn(
						"Node {} reported that it now uses version {}, but it's not the latest version {}. Application schema sync will be skipped",
						payload.getNodeId(), payload.getVersion(), farmConfig.getVersion());
				return null;
			}

			// Update Apps schema in DB
			Set<String> nodeTags = getNodesTagSet(payload.getNodeId());
			Set<AppDefConfig> appDefs = FarmConfigTools.findAppDefConfigByTags(nodeTags, farmConfig);
			List<AppInstanceRow> oldApps = appInstanceService
					.query(PagerParams.ALL, Query.n().eq(AppInstanceRow.FN_NODE_ID, payload.getNodeId())).getItems();
			removeOutdatedAppsFromDb(appDefs, oldApps);
			addNewAppsToDb(payload.getNodeId(), appDefs, oldApps);

			// NOTE: I could've attached app instances to config version and in that case I
			// wouldn't have to delete apps that are no longer mentioned in FarmConfig. BUT
			// in that case I'd have to migrate retained applications somehow to the new
			// version. Sounds a bit complicated, thence decided to leave logic as is
		} catch (Throwable t) {
			log.warn("Failed to process NodeNowUsesConfigReport: " + payload, t);
		}
		return null;
	}

	private void addNewAppsToDb(String nodeId, Set<AppDefConfig> appDefs, List<AppInstanceRow> oldApps) {
		Set<String> oldAppsNames = oldApps.stream().map(x -> x.getName()).collect(Collectors.toSet());
		appDefs.stream().filter(x -> !oldAppsNames.contains(x.getName())).forEach(appDef -> {
			appDef.getPrototypes().values().forEach(prototype -> {
				try {
					AppInstanceRow n = new AppInstanceRow();
					n.setId(IdTools.randomId());
					n.setName(appDef.getName());
					n.setPrototype(prototype.getName());
					n.setNodeId(nodeId);
					n.setStatusSchema(appDef.getStatusSchema());
					n = appInstanceService.create(n);
					log.debug("Added app instance {}, name {}, prototype {} to DB for node {}", n.getId(),
							appDef.getName(), prototype.getName(), nodeId);
				} catch (Throwable t) {
					throw new RuntimeException("Failed to add app " + appDef.getName() + " prototype "
							+ prototype.getName() + " to DB for node " + nodeId, t);
				}
			});
		});
	}

	private void removeOutdatedAppsFromDb(Set<AppDefConfig> appDefs, List<AppInstanceRow> oldApps) {
		Set<String> appDefNames = appDefs.stream().map(x -> x.getName()).collect(Collectors.toSet());
		oldApps.stream().filter(x -> !appDefNames.contains(x.getName())).forEach(x -> {
			try {
				appInstanceService.deleteById(x.getId());
				log.debug("Deleted outdated app instance Id {}, app {}, prototype {}, from node {}", x.getId(),
						x.getName(), x.getPrototype(), x.getNodeId());
			} catch (EntityNotFoundException e) {
				log.debug("Tried to delete app instance " + x.getId() + " from DB, but it wasn't there already", e);
			} catch (Throwable t) {
				throw new RuntimeException("Failed to delete outdated app " + x.getName() + " instance "
						+ x.getCreatedAt() + "  of node " + x.getNodeId(), t);
			}
		});
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public SmStateKind getKind() {
		return SmStateKind.Intermediate;
	}
}
