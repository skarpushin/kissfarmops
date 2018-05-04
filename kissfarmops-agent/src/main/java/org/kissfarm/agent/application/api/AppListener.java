package org.kissfarm.agent.application.api;

import org.kissfarm.agent.action_executor.api.ActionExecutionSpi;
import org.kissfarm.shared.config.dto.ActionStatus;

public interface AppListener {

	void onActionStatusChanged(AppInstanceSpi appInstanceSpi, ActionExecutionSpi actionExecutionSpi,
			ActionStatus status);

}
