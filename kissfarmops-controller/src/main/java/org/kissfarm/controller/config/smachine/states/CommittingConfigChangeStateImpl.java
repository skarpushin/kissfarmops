package org.kissfarm.controller.config.smachine.states;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.kissfarm.controller.config.smachine.dtos.FarmConfigMachineVariables;
import org.kissmachine.api.dto.SmStateData;
import org.kissmachine.api.machine.SmTransitionToState;
import org.kissmachine.api.machine.StateMachine;
import org.kissmachine.api.state.SmStateKind;
import org.kissmachine.impl.state.Void2;
import org.springframework.messaging.Message;

public class CommittingConfigChangeStateImpl extends FarmConfigStateAbstract<Void2, Void2, Void2, FarmConfigMachineVariables> {
	public static final String NAME = "CommittingConfigChange";

	@Override
	protected SmTransitionToState handleInitStateAction(Message<SmStateData> message, StateMachine stateMachine) {
		super.handleInitStateAction(message, stateMachine);

		// TBD: Notify nodes
		
		if (vars().getActiveWorkTree() != null) {
			FileUtils.deleteQuietly(new File(vars().getActiveWorkTree()));
		}
		vars().setActiveWorkTree(vars().getStagingWorkTree());
		vars().setActiveVersion(vars().getStagingVersion());
		vars().setStagingWorkTree(null);
		vars().setStagingVersion(null);

		return new SmTransitionToState("TBD: N node(s) confirmed switch to new config. " + vars().getActiveWorkTree(),
				false, ReadyStateImpl.NAME, null);
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
