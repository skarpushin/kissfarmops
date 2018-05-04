package org.kissfarm.agent.action_executor.impl_folder;

public class ResumeStatusChecksDeferedHandler implements ResumeMethod {
	private static final long serialVersionUID = -949433038782640343L;
	private ActionExecutionSpiImpl impl;

	@Override
	public void run() {
		impl.scheduleStatusCheck();
	}

	@Override
	public void setImpl(ActionExecutionSpiImpl impl) {
		this.impl = impl;
	}
}
