package org.kissfarmops.agent.process_execution.api;

public interface ProcessExecutionCallback {
	void onOutput(byte[] output);

	void onError(byte[] error);

	void onProcessFinished(int exitCode);
}
