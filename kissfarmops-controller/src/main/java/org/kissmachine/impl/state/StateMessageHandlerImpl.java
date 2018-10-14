package org.kissmachine.impl.state;

import java.util.function.Function;

import org.kissmachine.api.machine.SmTransitionToState;
import org.springframework.messaging.Message;

public class StateMessageHandlerImpl<T> implements StateMessageHandler<T> {
	private Class<T> payloadClazz;
	private Function<Message<T>, SmTransitionToState> handler;

	public StateMessageHandlerImpl(Class<T> payloadClazz, Function<Message<T>, SmTransitionToState> handler) {
		this.payloadClazz = payloadClazz;
		this.handler = handler;
	}

	@Override
	public boolean test(Message<?> t) {
		return t.getPayload() != null && payloadClazz.isAssignableFrom(t.getPayload().getClass());
	}

	@Override
	public SmTransitionToState apply(Message<T> t) {
		return handler.apply(t);
	}
}
