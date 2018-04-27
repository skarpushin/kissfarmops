package org.kissfarmops.agent.action_executor.api;

/**
 * Interface to control and get info about action execution that was already
 * started
 * 
 * @author Sergey Karpushin
 *
 */
public interface ActionExecutionSpi {
	String getName();

	/**
	 * @return Unique identifier of this execution (unique throughout the system)
	 */
	String getExecutionId();

	/**
	 * @return action's status
	 */
	ActionStatus getStatus();

	/**
	 * @return action result (if any). This field expects to contain a serialized
	 *         json data that was created as a result of action execution. The
	 *         reason it's in String format -- we have nothing to do with result, we
	 *         just need to transfer it to the server, Which also has nothing to do
	 *         with it's contents. Only workflow manager supposed to know how to
	 *         deserialize and handle this data.
	 */
	String getResult();

	/**
	 * @return output text printed to stdout and/or stderr
	 */
	String getOutput();

	/**
	 * @return true if termination succeeded or action wasn't running. False
	 *         otherwise.
	 */
	boolean terminate();

	/**
	 * Stop working on this action as soon as possible and prepare for
	 * reconciliation and resume later.
	 * 
	 * This is NON_BLOCKING method. It will instruct action to suspend but will not
	 * interrupt or interrupt current process if any. You should listen to callback
	 * invocations of {@link ActionsExecutionListener} for status change
	 * 
	 * This method supposed to be called when agent received termination signal and
	 * we need to wrap ASAP, but we need to avoid loosing any current progress.
	 * 
	 * For sync action it means report will be sent to the controller after
	 * reconciliation.
	 * 
	 * For async action it means status checks will continue after restart.
	 * 
	 * Even if action failed the info about it will be reported after
	 * reconciliation.
	 */
	void suspend();
}
