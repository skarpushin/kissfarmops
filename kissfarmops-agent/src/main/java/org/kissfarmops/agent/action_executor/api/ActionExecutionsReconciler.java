package org.kissfarmops.agent.action_executor.api;

import java.util.Set;

/**
 * This interface is used to reconcile list of current action executions. It's
 * meant to be invoked after agent was started
 * 
 * @author Sergey Karpushin
 *
 */
public interface ActionExecutionsReconciler {
	/**
	 * @return list of executions that are currently being observed. Some of them
	 *         might have been completed already. Each instance will call
	 *         {@link ActionsExecutionListener} once instantiated and report current
	 *         status.
	 */
	Set<ActionExecutionSpi> reconcileExecutions();
}
