package org.kissfarm.controller.config.smachine.states;

import org.kissfarm.controller.config.smachine.dtos.FarmConfigMachineVariables;
import org.kissmachine.api.dto.SmStateData;
import org.kissmachine.api.machine.SmTransitionToState;
import org.kissmachine.api.machine.StateMachine;
import org.kissmachine.api.state.SmStateKind;
import org.kissmachine.impl.state.Void2;
import org.springframework.messaging.Message;

public class TearDownMessageFlowStateImpl extends FarmConfigStateAbstract<Void2, Void2, Void2, FarmConfigMachineVariables> {
	public static final String NAME = "TearDownMessageFlow";

	@Override
	protected SmTransitionToState handleInitStateAction(Message<SmStateData> message, StateMachine stateMachine) {
		super.handleInitStateAction(message, stateMachine);


		return new SmTransitionToState("TBD: No flows to tear down", false, CommittingConfigChangeStateImpl.NAME, null);
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
