package org.kissfarm.agent.application.api;

import java.util.Map;

import org.kissfarm.agent.action_executor.api.ActionExecutionSpi;
import org.kissfarmops.shared.config.api.AppDefinitionConfig;
import org.kissfarmops.shared.config.api.AppPrototypeConfig;

/**
 * SPI for interracting with 1 application of a particular version
 * 
 * @author Sergey Karpushin
 *
 */
public interface AppInstanceSpi {
	String getVersion();

	AppDefinitionConfig getAppDefinitionConfig();

	AppPrototypeConfig getAppPrototypeConfig();

	/**
	 * This method will instruct {@link AppInstanceSpi} to callback when no action
	 * is executed. It can be immediately OR it will happen after all current
	 * actions are finished
	 */
	void callWhenNoActionsAreBeingExecuted(Runnable callback);

	/**
	 * Invoke an action define in this application
	 * 
	 * NOTE: Even "getStatus" action will be triggered from controller
	 */
	ActionExecutionSpi invokeAction(String actionName, String executionId, Map<String, String> parameters);

	void suspend();
}
