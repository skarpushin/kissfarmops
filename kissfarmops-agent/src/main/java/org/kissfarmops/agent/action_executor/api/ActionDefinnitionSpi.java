package org.kissfarmops.agent.action_executor.api;

import java.util.Map;

/**
 * Interface used to get information about action and to invoke this action
 * 
 * @author Sergey Karpushin
 *
 */
public interface ActionDefinnitionSpi {
	String getName();

	/**
	 * 
	 * @param envVars
	 *            map of key-values which can hold action's parameter values as well
	 *            as environment variables override
	 * 
	 * @return interface that can be used to get execution status and results (when
	 *         available). Important: this interface is not supposed to be polled to
	 *         check if action is completed. Impl of this interface is supposed to
	 *         notify injected {@link ActionsExecutionListener} about action
	 *         execution status
	 */
	ActionExecutionSpi executeAction(Map<String, String> envVars);

	/*
	 * NOTE: Initially I added these fields and although this information is
	 * available I don't see why would we want to expose it
	 * 
	 * Map<String, String> getEnvVars();
	 * 
	 * List<String> getParameterNames();
	 */
}
