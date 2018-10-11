package org.kissmachine.impl.state;

import java.util.function.Function;
import java.util.function.Predicate;

import org.kissmachine.api.machine.SmTransitionToState;
import org.springframework.messaging.Message;

public interface StateMessageHandler<T> extends Predicate<Message<?>>, Function<Message<T>, SmTransitionToState> {

}
