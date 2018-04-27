package org.kissfarmops.agent.action_executor.api;

public enum ActionStatus {
	/**
	 * Action was just created, not ready for work yet. Supposedly this status will
	 * never be exposed to consumer
	 */
	Initializing,

	/**
	 * Action is currently invoked (process started) waiting for it to complete
	 */
	InProgressSync,

	/**
	 * Action was invoked and returned code that indicates this is an async action.
	 * Status of this action will be checked periodically using other script
	 */
	InProgressAsync,

	/**
	 * Action exit code indicated success
	 */
	Success,

	/**
	 * Action exit code indicated failure
	 */
	Failed,

	/**
	 * Unhandled exception (normally due to severe conditions) happened while
	 * executing thins action.
	 */
	Exception,

	/**
	 * Action invocation or action status checked timedout. Process took more time
	 * than it was expected
	 */
	Timedout,

	/**
	 * Action was terminated due to user/consumer request
	 */
	Terminated,

	/**
	 * Suspended means action was gracefully suspended and ready to be continued
	 * after reconciliation
	 */
	Suspended,

	/**
	 * Means action was never started. Status used to handle edge case when agent
	 * received action invocation requested during restart sequence. This status
	 * means action is planned (all data is prepared) but will be executed once
	 * reconciled.
	 */
	Postponed
}