package org.kissfarmops.agent.application.api;

import org.kissfarmops.agent.action_executor.api.ActionExecutionSpi;
import org.kissfarmops.agent.action_executor.api.ActionStatus;

public interface AppListener {

	void onActionStatusChanged(AppInstanceSpi appInstanceSpi, ActionExecutionSpi actionExecutionSpi,
			ActionStatus status);

}
