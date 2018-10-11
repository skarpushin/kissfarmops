package org.kissfarm.controller.config.smachine.states;

import java.io.File;

import org.kissfarm.controller.config.api.FarmConfig;
import org.kissfarm.controller.config.api.FarmConfigFolderReader;
import org.kissfarm.controller.config.api.GitAbstraction;
import org.kissfarm.controller.config.api.GitConfig;
import org.kissfarm.controller.config.smachine.dtos.FarmConfigMachineVariables;
import org.kissmachine.api.dto.SmStateData;
import org.kissmachine.api.machine.SmTransitionToState;
import org.kissmachine.api.machine.StateMachine;
import org.kissmachine.api.state.SmStateKind;
import org.kissmachine.impl.state.SmStateAbstract;
import org.kissmachine.impl.state.Void2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.messaging.Message;
import org.summerb.utils.exceptions.translator.ExceptionTranslator;

import com.google.common.base.Preconditions;

/**
 * This state is responsible for initial download of the config
 * 
 * @author Sergey Karpushin
 *
 */
public class ClonningConfigStateImpl extends SmStateAbstract<GitConfig, Void2, Void2, FarmConfigMachineVariables> {
	public static final String NAME = "ClonningConfig";

	@Autowired
	private GitAbstraction gitAbstraction;
	@Autowired
	private ExceptionTranslator exceptionTranslator;
	@Autowired
	private FarmConfigFolderReader farmConfigFolderReader;

	private String workingPath;

	/**
	 * @param workingPath folder that can be used as a parent folder for all
	 *                    configuration we going to download now and an the future
	 */
	public ClonningConfigStateImpl(String workingPath) {
		this.workingPath = workingPath;
	}

	@Override
	protected SmTransitionToState handleInitStateAction(Message<SmStateData> message, StateMachine stateMachine) {
		super.handleInitStateAction(message, stateMachine);

		try {
			// Verify connection info
			GitConfig connectionInfo = getParams();
			String remoteVersion = gitAbstraction.getRemoteVersion(connectionInfo);
			vars().setGitConfig(connectionInfo);

			// Download config
			String newWorkingTreeFolder = chooseNewWorkingTreeFolder(remoteVersion);
			gitAbstraction.cloneRepo(connectionInfo, newWorkingTreeFolder);

			// Parse config
			FarmConfig farmConfig = farmConfigFolderReader.readFarmConfig(new File(newWorkingTreeFolder),
					remoteVersion);
			Preconditions.checkState(farmConfig != null, "FarmConfig read failure");
			Preconditions.checkState(farmConfig.getAppDefs().size() > 0, "No apps found in config");
			vars().setStagingWorkTree(newWorkingTreeFolder);
			vars().setStagingVersion(remoteVersion);

			return new SmTransitionToState("Config clonned. Location: " + newWorkingTreeFolder, false,
					DeliveringConfigToNodesStateImpl.NAME, null);
		} catch (Throwable t) {
			log.error("Failed to clone new config repo", t);
			return new SmTransitionToState(
					"Exception: " + exceptionTranslator.buildUserMessage(t, LocaleContextHolder.getLocale()), false,
					NoConfigStateImpl.NAME, null);
		}
	}

	private String chooseNewWorkingTreeFolder(String version) {
		return workingPath + File.separator + getStateData().getId() + " - " + version;
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
