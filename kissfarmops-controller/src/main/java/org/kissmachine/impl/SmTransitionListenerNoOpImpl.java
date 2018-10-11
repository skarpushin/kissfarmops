package org.kissmachine.impl;

import org.kissmachine.api.machine.SmTransitionListener;
import org.kissmachine.api.machine.StateMachine;
import org.kissmachine.api.state.SmState;

public class SmTransitionListenerNoOpImpl implements SmTransitionListener {

	@Override
	public void onTransition(StateMachine machine, SmState from, SmState to) {
		// no op
	}

}
