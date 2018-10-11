package org.kissmachine.api.machine;

import java.io.Serializable;

import org.kissmachine.api.dto.SmData;

/**
 * Methods of this interface supposed to be used only by
 * {@link StateMachineBuilder} for the purpose of initializing impl of
 * {@link StateMachine}
 * 
 * @author Sergey Karpushin
 *
 */
public interface StateMachineKickstarter {

	/**
	 * @param initialStateParams machine input parameters
	 * @param machineVars        TODO
	 * @return Machine ID
	 * @throws IllegalStateException if machine is in terminal state or already
	 *                               running
	 */
	String startNew(Serializable initialStateParams, Serializable machineVars) throws IllegalStateException;

	/**
	 * Resume previously saved machine state. This operation is only possible if
	 * storage facilities are available
	 * 
	 * @param instanceId machine instance id
	 * @throws IllegalStateException if machine already started
	 */
	void resumeExisting(String instanceId) throws IllegalStateException;

	void resumeExisting(SmData existingMachine) throws IllegalStateException;

}
