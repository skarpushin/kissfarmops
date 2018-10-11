package org.kissfarm.controller.config.smachine.states;

import java.io.File;

import org.kissfarm.controller.config.api.FarmConfig;
import org.kissfarm.controller.config.api.FarmConfigFolderReader;
import org.kissfarm.controller.config.api.GitConfig;
import org.kissfarm.controller.config.smachine.dtos.FarmConfigMachineVariables;
import org.kissfarm.controller.config.smachine.dtos.PullConfigUpdateRequest;
import org.kissmachine.api.dto.SmStateData;
import org.kissmachine.api.machine.SmTransitionToState;
import org.kissmachine.api.machine.StateMachine;
import org.kissmachine.api.state.SmStateKind;
import org.kissmachine.impl.state.SmStateAbstract;
import org.kissmachine.impl.state.Void2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;

import com.google.common.base.Preconditions;

/**
 * This state is responsible for initial download of the config
 * 
 * @author Sergey Karpushin
 *
 */
public class ReadyStateImpl extends SmStateAbstract<GitConfig, Void2, Void2, FarmConfigMachineVariables> {
	public static final String NAME = "Ready";

	@Autowired
	private FarmConfigFolderReader farmConfigFolderReader;

	public ReadyStateImpl() {
		handleByPayload(PullConfigUpdateRequest.class, msg -> onPullRequest(msg));
		handleByPayload(GitConfig.class, msg -> onUpdateGitConfig(msg.getPayload()));
		// TODO: Respond to config requests from nodes
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
			FarmConfig farmConfig = farmConfigFolderReader.readFarmConfig(new File(vars().getActiveWorkTree()),
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

	private SmTransitionToState onPullRequest(Message<PullConfigUpdateRequest> msg) {
		return new SmTransitionToState("Received request to check for updates", false, PullingUpdatesStateImpl.NAME,
				null);
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
