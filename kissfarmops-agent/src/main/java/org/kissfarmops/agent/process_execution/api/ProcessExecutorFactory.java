package org.kissfarmops.agent.process_execution.api;

import java.util.Map;

/**
 * Impl of this interface will execute command and provide means of capturing
 * output of this process and channel input (if needed)
 * 
 * It's is actually very generic, not specific to this project.
 * 
 * @author Sergey Karpushin
 *
 */
public interface ProcessExecutorFactory {
	ProcessExecution execute(String workingDirectory, Map<String, String> envVariables,
			ProcessExecutionCallback callback, String... command);
}
