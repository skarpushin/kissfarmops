package org.kissmachine.api.machine;

import org.kissmachine.api.dto.SmData;
import org.kissmachine.api.state.SmState;
import org.springframework.messaging.Message;

/**
 * "Keep It Simple Stupid" implementation for the state machine. There is
 * supposed to be only one re-usable (default) implementation.
 * 
 * NOTE: I've considered to use Spring's Statemachine sub-project, but it turned
 * out to be seemingly over-engineered, cluttered and at the same time not
 * flexible enough in terms of state types and action types. Didn't look like a
 * real-world-oriented tool. And lastly, that was a huge turn off for me (quote
 * from official reference) "Building a StateMachineContext and then restoring a
 * state machine from it has always been a little bit of a black magic if done
 * manually". I hate magic as it is especially black magic. In this project I
 * need something very simple, concise and effective. Therefore I've decided to
 * implement simple State machine myself.
 * 
 * @author Sergey Karpushin
 *
 */
public interface StateMachine {

	/**
	 * @return Some user-defined machine type
	 */
	String getMachineType();

	/**
	 * @return current machine data. Impl will return it as-is. Do not modify it
	 *         unless you do it from State logic (impl of {@link SmState}
	 */
	SmData getMachineData();

	/**
	 * @return current machine state. Impl will return it as-is. Do not modify it
	 *         unless you do it from State logic (impl of {@link SmState}
	 */
	SmState getCurrentState();

	Throwable getMachineExceptionIfAny();

	/**
	 * Send message to the machine. Machine will forward it to the current state and
	 * latter might decide if machine will be switched from other state.
	 * 
	 * Since underlying implementation could be async there is no immediate result
	 * of this message. Use {@link SmTransitionListener} if needed to listen for
	 * state changes
	 * 
	 * @param message
	 */
	<T> void sendEvent(Message<T> message);

	void stop();

}
