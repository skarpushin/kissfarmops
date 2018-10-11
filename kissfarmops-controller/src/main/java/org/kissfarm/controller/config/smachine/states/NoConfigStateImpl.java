package org.kissfarm.controller.config.smachine.states;

import org.kissfarm.controller.config.api.GitConfig;
import org.kissfarm.controller.config.smachine.dtos.FarmConfigMachineVariables;
import org.kissmachine.api.dto.SmStateData;
import org.kissmachine.api.machine.SmTransitionToState;
import org.kissmachine.api.machine.StateMachine;
import org.kissmachine.api.state.SmStateKind;
import org.kissmachine.impl.state.SmStateAbstract;
import org.kissmachine.impl.state.Void2;
import org.springframework.messaging.Message;

/**
 * The very initial state of this machine when controller was never configured
 * and just waiting for the farm config to be provided
 * 
 * @author Sergey Karpushin
 *
 */
public class NoConfigStateImpl extends SmStateAbstract<Void2, Void2, GitConfig, FarmConfigMachineVariables> {
	public static final String NAME = "noConfig";

	public NoConfigStateImpl() {
		handleByPayload(GitConfig.class, msg -> onGitConfig(msg));
	}

	@Override
	protected SmTransitionToState handleInitStateAction(Message<SmStateData> message, StateMachine stateMachine) {
		super.handleInitStateAction(message, stateMachine);
		vars().setActiveVersion(null);
		vars().setGitConfig(null);
		vars().setStagingWorkTree(null);
		vars().setActiveWorkTree(null);
		return null;
	}

	private SmTransitionToState onGitConfig(Message<GitConfig> msg) {
		setResult(msg.getPayload());
		return new SmTransitionToState("GIT repo configuration received", false, ClonningConfigStateImpl.NAME,
				getResult());
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public SmStateKind getKind() {
		return SmStateKind.Initial;
	}
}
