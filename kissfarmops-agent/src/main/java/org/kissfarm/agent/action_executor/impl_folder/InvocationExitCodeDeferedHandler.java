package org.kissfarm.agent.action_executor.impl_folder;

import org.kissfarm.agent.action_executor.api.ResumeMethod;

public class InvocationExitCodeDeferedHandler implements ResumeMethod {
	private static final long serialVersionUID = -2023399054231328176L;

	private transient ActionExecutionSpiImpl impl;

	private int exitCode;

	public InvocationExitCodeDeferedHandler(int exitCode) {
		this.exitCode = exitCode;
	}

	public InvocationExitCodeDeferedHandler() {
		// Constructor for IO only
	}

	@Override
	public void run() {
		impl.handleInvocationExitCode(exitCode);
	}

	public int getExitCode() {
		return exitCode;
	}

	public void setExitCode(int exitCode) {
		this.exitCode = exitCode;
	}

	public void setImpl(ActionExecutionSpiImpl impl) {
		this.impl = impl;
	}
}
