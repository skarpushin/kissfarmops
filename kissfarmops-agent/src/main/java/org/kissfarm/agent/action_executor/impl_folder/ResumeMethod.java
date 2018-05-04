package org.kissfarm.agent.action_executor.impl_folder;

import java.io.Serializable;

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
