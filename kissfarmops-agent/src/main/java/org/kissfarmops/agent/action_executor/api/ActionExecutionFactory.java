package org.kissfarmops.agent.action_executor.api;

import org.kissfarmops.agent.action_executor.impl_folder.ActionInvocationInfo;

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
