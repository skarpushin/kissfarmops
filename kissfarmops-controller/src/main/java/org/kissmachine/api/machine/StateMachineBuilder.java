package org.kissmachine.api.machine;

import java.io.Serializable;

/**
 * This is a factory interface for state machines. Each specific workflow is
 * expected to be represented as an implementation of this interface.
 * 
 * @author Sergey Karpushin
 *
 */
public interface StateMachineBuilder {

	String getMachineType();

	StateMachine buildNew(Serializable params);

	StateMachine resume(String machineInstanceId);

}
