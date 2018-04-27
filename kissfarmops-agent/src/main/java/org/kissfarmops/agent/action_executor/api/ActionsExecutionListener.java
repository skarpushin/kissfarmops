package org.kissfarmops.agent.action_executor.api;

public interface ActionsExecutionListener {
	/**
	 * This method will be called to report on action status change. Impl is
	 * expected to deliver this information back to controller. Impl must gather all
	 * information needed during this call. After this method will end
	 * ActionExecutionSpi might become unusable
	 */
	void onActionStatusChanged(ActionExecutionSpi actionExecutionSpi, ActionStatus oldStatus, ActionStatus newStatus);
}
