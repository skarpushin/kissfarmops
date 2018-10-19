package org.kissmachine.impl;

import java.util.concurrent.CompletableFuture;

import org.kissmachine.api.machine.SmEventDeliveryStrategy;
import org.kissmachine.api.machine.SmTransitionToState;
import org.kissmachine.api.machine.StateMachine;
import org.kissmachine.api.state.SmState;
import org.springframework.messaging.Message;
import org.springframework.transaction.annotation.Transactional;

/**
 * Very simple impl which performs message delivery immediately
 * 
 * @author Sergey Karpushin
 *
 */
public class SmEventDeliveryStrategySyncImpl implements SmEventDeliveryStrategy {
	@Override
	@Transactional(rollbackFor = Throwable.class)
	public <T> CompletableFuture<SmTransitionToState> sendEvent(Message<T> message, StateMachine stateMachine) {
		synchronized (stateMachine.getMachineType()) {
			SmState currentState = stateMachine.getCurrentState();
			try {
				SmTransitionToState result = currentState.onMessage(message);
				return CompletableFuture.completedFuture(result);
			} catch (Throwable t) {
				CompletableFuture<SmTransitionToState> ret = new CompletableFuture<>();
				ret.completeExceptionally(t);
				return ret;
			}
		}
	}
}
