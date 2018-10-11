package org.kissmachine.api.state;

import org.kissmachine.api.dto.SmStateData;
import org.kissmachine.api.machine.SmTransitionToState;
import org.kissmachine.api.machine.StateMachine;
import org.springframework.messaging.Message;

/**
 * Impl of this interface will represent certain state within machine/workflow.
 * 
 * This interface is designed to be used mostly by {@link StateMachine} impl, to
 * perform logic and transition between states
 * 
 * @author Sergey Karpushin
 *
 */
public interface SmState {

	/**
	 * Name of the state, unique per workflow definition
	 */
	String getName();

	SmStateKind getKind();

	/**
	 * Get {@link SmStateData} that is used to represent the serializable data of
	 * this state
	 * 
	 * NOTE: This is injected into state via {@link InitStateAction} or
	 * {@link ResumeStateAction} event message {@link #onMessage(Message)}
	 * 
	 * @return current {@link SmStateData}. If calling outside of State Machine, Use
	 *         it for read only purpose
	 */
	SmStateData getStateData();

	void setStateData(SmStateData smStateData);

	/**
	 * Handle message. Machine will call this method when entering state with
	 * {@link InitStateAction} payload and {@link ResumeStateAction} when resuming
	 * it.
	 * 
	 * Also if state has a async nature then there might be more message coming
	 * which will affect state logic and possible machine state transitions
	 * 
	 * @return state transition info, or null if transition is not required
	 */
	<T> SmTransitionToState onMessage(Message<T> event);

	/**
	 * Stop state activity. State's data can be update during this call. In this
	 * case machine will try to save this state (if persistence is configured)
	 */
	void stop();
}
