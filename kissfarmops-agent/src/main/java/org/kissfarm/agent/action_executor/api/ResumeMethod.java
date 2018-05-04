package org.kissfarm.agent.action_executor.api;

import java.io.Serializable;

import org.kissfarm.agent.action_executor.impl_folder.ActionExecutionSpiImpl;

/**
 * Base interface for methods on how Action will proceed after reconciliation
 * 
 * @author Sergey Karpushin
 *
 */
public interface ResumeMethod extends Runnable, Serializable {
	void setImpl(ActionExecutionSpiImpl impl);

	void run();
}
