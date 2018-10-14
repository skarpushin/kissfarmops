package org.kissmachine.impl.state;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.kissmachine.api.dto.SmStateData;
import org.kissmachine.api.machine.SmTransitionToState;
import org.kissmachine.api.machine.StateMachine;
import org.kissmachine.api.machine.StateMachineBuilder;
import org.kissmachine.api.state.InitStateAction;
import org.kissmachine.api.state.ResumeStateAction;
import org.kissmachine.api.state.SmState;
import org.kissmachine.api.state.SmStateKind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;

/**
 * This abstract class is designed to make it more convenient to develop custom
 * implementation of {@link SmState}
 * 
 * @author Sergey Karpushin
 *
 * @param <P>
 * @param <S>
 * @param <R>
 * @param <M> type of Machine-wide state (variables)
 */
public abstract class SmStateAbstract<P extends Serializable, S extends Serializable, R extends Serializable, M extends Serializable>
		implements SmState {
	protected Logger log = LoggerFactory.getLogger(getClass());

	protected SmStateData stateData;

	/**
	 * This back reference to state machine might be used to send events in case
	 * state discovered something. All state changes must go through standard
	 * message entry point, which is
	 * {@link StateMachine#sendEvent(org.springframework.messaging.Message)}
	 */
	protected StateMachine stateMachine;

	private P params;
	private S state;
	private R result;

	private List<StateMessageHandler<?>> handlers = new ArrayList<>();

	public SmStateAbstract() {
		handleByPayload(InitStateAction.class, msg -> {
			Message<SmStateData> msg2 = MessageBuilder.withPayload(msg.getPayload().getStateData())
					.copyHeaders(msg.getHeaders()).build();
			return handleInitStateAction(msg2, msg.getPayload().getStateMachine());
		});

		handleByPayload(ResumeStateAction.class, msg -> {
			Message<SmStateData> msg2 = MessageBuilder.withPayload(msg.getPayload().getStateData())
					.copyHeaders(msg.getHeaders()).build();
			return handleResumeStateAction(msg2, msg.getPayload().getStateMachine());
		});
	}

	/**
	 * Override this method to implement logic on state entrance
	 * 
	 * @param stateMachine2
	 * 
	 * @param smStateData   initial state data
	 * @return null if machine state shouldn't change. Or transition info if it
	 *         should
	 */
	protected SmTransitionToState handleInitStateAction(Message<SmStateData> message, StateMachine stateMachine) {
		this.stateMachine = stateMachine;
		setStateData(message.getPayload());
		return null;
	}

	/**
	 * Override this method to implement logic on state entrance after it was
	 * deserialized
	 * 
	 * @param message       will contain {@link InitStateAction} with initial state
	 *                      data
	 * @param stateMachine2
	 * @return null if machine state shouldn't change. Or transition info if it
	 *         should
	 */
	protected SmTransitionToState handleResumeStateAction(Message<SmStateData> message, StateMachine stateMachine) {
		// by default Resume has same behavior as Init. Subclass can override it.
		return handleInitStateAction(message, stateMachine);
	}

	/**
	 * Convenience method for subclass to register handle for specific payload type
	 * 
	 * @param clazz   expected class of message's payload
	 * @param handler the handler for that message payload type
	 */
	protected <T> void handleByPayload(Class<T> clazz, Function<Message<T>, SmTransitionToState> handler) {
		handlers.add(new StateMessageHandlerImpl<T>(clazz, handler));
	}

	@Override
	public <T> SmTransitionToState onMessage(Message<T> event) {
		@SuppressWarnings("unchecked")
		StateMessageHandler<T> handler = (StateMessageHandler<T>) handlers.stream().filter(x -> x.test(event))
				.findFirst().orElse(null);
		if (handler == null) {
			log.warn("{} wont handle message {}", this, event.getPayload());
			return handleDeadLetter(event);
		}

		return handler.apply(event);
	}

	protected <T> SmTransitionToState handleDeadLetter(Message<T> event) {
		// Subclass might want to impl that
		return null;
	}

	@Override
	public SmStateData getStateData() {
		// NOTE: To make it easier for the subclass to manage State variables we wrap it
		// here.
		stateData.setParams(getParams());
		stateData.setState(getState());
		stateData.setResult(getResult());
		return stateData;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setStateData(SmStateData smStateData) {
		stateData = smStateData;
		params = (P) stateData.getParams();
		state = (S) stateData.getState();
		result = (R) stateData.getResult();
	}

	@Override
	public abstract String getName();

	@Override
	public abstract SmStateKind getKind();

	@Override
	public void stop() {
		// default behavior -- no op
	}

	/**
	 * Set back ref to State Machine. Expected to be called by impl of
	 * {@link StateMachineBuilder}
	 */
	public void setStateMachine(StateMachine stateMachine) {
		this.stateMachine = stateMachine;
	}

	/**
	 * Convenience shortcut/method to get machine-wide variables
	 */
	protected M vars() {
		@SuppressWarnings("unchecked")
		M vars = (M) stateMachine.getMachineData().getVars();
		return vars;
	}

	/**
	 * @return params used to engage this state
	 */
	public P getParams() {
		return params;
	}

	public void setParams(P params) {
		this.params = params;
	}

	/**
	 * @return state data used by this State to perform it's async logic. When State
	 */
	public S getState() {
		return state;
	}

	public void setState(S state) {
		this.state = state;
	}

	/**
	 * @return result of this state
	 */
	public R getResult() {
		return result;
	}

	public void setResult(R result) {
		this.result = result;
	}

}
