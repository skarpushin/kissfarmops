package org.kissfarmops.agent.action_executor.impl_folder;

import java.util.concurrent.atomic.AtomicReference;

import org.kissfarmops.agent.process_execution.api.ProcessExecutionCallback;

public class ProcessExecutionCallbackRedirect implements ProcessExecutionCallback {
	private AtomicReference<ProcessExecutionCallback> redirectTo;

	public ProcessExecutionCallbackRedirect(ProcessExecutionCallback redirectTo) {
		this.redirectTo = new AtomicReference<ProcessExecutionCallback>(redirectTo);
	}

	@Override
	public void onOutput(byte[] output) {
		redirectTo.get().onOutput(output);
	}

	@Override
	public void onError(byte[] error) {
		redirectTo.get().onError(error);
	}

	@Override
	public void onProcessFinished(int exitCode) {
		redirectTo.get().onProcessFinished(exitCode);
	}

	public void setRedirectTo(ProcessExecutionCallback redirectTo) {
		this.redirectTo.set(redirectTo);
	}
}