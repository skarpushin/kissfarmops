package org.kissfarmops.agent.action_executor.impl_folder;

/**
 * This used when suspending action. Instance will be saved on disk as a next
 * step to be performed. After reconciliation it will continue
 * 
 * @author Sergey Karpushin
 *
 */
public class StatusCheckExitCodeDeferedHandler implements ResumeMethod {
	private static final long serialVersionUID = 6313470595574103617L;

	private transient ActionExecutionSpiImpl impl;

	private int exitCode;

	public StatusCheckExitCodeDeferedHandler(int exitCode) {
		this.exitCode = exitCode;
	}

	public StatusCheckExitCodeDeferedHandler() {
		// Constructor for IO only
	}

	@Override
	public void run() {
		impl.handleStatusCheckExitCode(exitCode);
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