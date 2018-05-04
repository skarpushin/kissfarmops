package org.kissfarm.agent.action_executor.api;

/**
 * Interface used to get information about action and to invoke this action
 * 
 * @author Sergey Karpushin
 *
 */
public interface ActionExecutionFactory {
	ActionExecutionSpi startNewInvocation(ActionInvocationInfo invocationInfo,
			ActionsExecutionListener actionsExecutionListener);

	ActionExecutionSpi postponeAction(ActionInvocationInfo invocationInfo,
			ActionsExecutionListener actionsExecutionListener);

	ActionExecutionSpi reconcileExistingAction(String instanceFolder,
			ActionsExecutionListener actionsExecutionListener);
}
