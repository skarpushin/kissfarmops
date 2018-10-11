package org.kissmachine.api.machine;

import org.kissmachine.api.state.SmState;

public interface SmTransitionListener {

	void onTransition(StateMachine machine, SmState from, SmState to);

}
