package org.kissfarm.agent.process_execution.impl;

import java.util.Map;

import org.kissfarm.agent.process_execution.api.ProcessExecution;
import org.kissfarm.agent.process_execution.api.ProcessExecutionCallback;
import org.kissfarm.agent.process_execution.api.ProcessExecutorFactory;

public class ProcessExecutorFactoryImpl implements ProcessExecutorFactory {
	@Override
	public ProcessExecution execute(String workingDirectory, Map<String, String> envVariables,
			ProcessExecutionCallback callback, String... command) {
		return new ProcessExecutionImpl(workingDirectory, envVariables, callback, command);
	}
}
