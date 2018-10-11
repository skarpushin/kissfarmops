package org.kissmachine.api.machine;

import java.util.concurrent.CompletableFuture;

import org.springframework.messaging.Message;

public interface SmEventDeliveryStrategy {

	<T> CompletableFuture<SmTransitionToState> sendEvent(Message<T> message, StateMachine stateMachine);

}
