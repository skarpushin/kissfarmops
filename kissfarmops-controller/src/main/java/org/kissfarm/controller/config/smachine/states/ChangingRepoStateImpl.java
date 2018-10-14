package org.kissfarm.controller.config.smachine.states;

import java.io.File;
import java.util.Locale;

import org.kissfarm.controller.config.api.FarmConfigFolderReader;
import org.kissfarm.controller.config.api.GitAbstraction;
import org.kissfarm.controller.config.dto.FarmConfig;
import org.kissfarm.controller.config.dto.GitConfig;
import org.kissfarm.controller.config.impl.FarmConfigTools;
import org.kissfarm.controller.config.smachine.dtos.FarmConfigMachineVariables;
import org.kissmachine.api.dto.SmStateData;
import org.kissmachine.api.machine.SmTransitionToState;
import org.kissmachine.api.machine.StateMachine;
import org.kissmachine.api.state.SmStateKind;
import org.kissmachine.impl.state.Void2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.summerb.utils.exceptions.translator.ExceptionTranslator;

import com.google.common.base.Preconditions;

/**
 * This state is responsible for relocating to other repo. At this point impl is
 * super similar to {@link PullingUpdatesStateImpl}, but later on we might want
 * to dismantle they whole storage related to old config. But for now we assume
 * that repo at new location is just a relocated storage
 * 
 * @author Sergey Karpushin
 *
 */
public class ChangingRepoStateImpl extends FarmConfigStateAbstract<GitConfig, Void2, Void2, FarmConfigMachineVariables> {
	public static final String NAME = "ChangingRepo";

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
	public ChangingRepoStateImpl(String workingPath) {
		this.workingPath = workingPath;
	}

	@Override
	protected SmTransitionToState handleInitStateAction(Message<SmStateData> message, StateMachine stateMachine) {
		super.handleInitStateAction(message, stateMachine);

		try {
			// Check remote version is newer
			String remoteVersion = gitAbstraction.getRemoteVersion(getParams());
			if (remoteVersion.equals(vars().getActiveVersion())) {
				vars().setGitConfig(getParams());
				return new SmTransitionToState("Relocated to new repo, but it has the same version: " + remoteVersion,
						false, ReadyStateImpl.NAME, null);
			}

			// Pull changes
			String newWorkingTreeFolder = chooseNewWorkingTreeFolder(remoteVersion);
			remoteVersion = gitAbstraction.cloneRepo(getParams(), newWorkingTreeFolder);

			// Validate
			validateNewFarmConfig(remoteVersion, newWorkingTreeFolder);
			vars().setStagingWorkTree(newWorkingTreeFolder);
			vars().setStagingVersion(remoteVersion);

			vars().setGitConfig(getParams());
			return new SmTransitionToState("Relocating to new repo: " + newWorkingTreeFolder, false,
					DeliveringConfigToNodesStateImpl.NAME, null);
		} catch (Throwable t) {
			log.error("Failed to pull config changes", t);
			return new SmTransitionToState("Exception: " + exceptionTranslator.buildUserMessage(t, Locale.ENGLISH),
					false, ReadyStateImpl.NAME, null);
		}
	}

	private void validateNewFarmConfig(String remoteVersion, String newWorkingTreeFolder) {
		FarmConfig oldFarmConfig = farmConfigFolderReader.readFarmConfig(new File(vars().getActiveWorkTree()),
				vars().getActiveVersion());
		FarmConfig newFarmConfig = farmConfigFolderReader.readFarmConfig(new File(newWorkingTreeFolder), remoteVersion);
		Preconditions.checkState(newFarmConfig != null, "FarmConfig read failure");
		Preconditions.checkState(newFarmConfig.getAppDefs().size() > 0, "No apps found in config");
		FarmConfigTools.assertFarmConfigChangeValid(oldFarmConfig, newFarmConfig);
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
