package org.kissfarmops.agent.action_executor.impl_folder;

import org.kissfarmops.agent.action_executor.api.ActionStatus;

public class InvocationTimeOutDeferedHandler implements ResumeMethod {
	private static final long serialVersionUID = -3903017322568765823L;

	private transient ActionExecutionSpiImpl impl;

	public InvocationTimeOutDeferedHandler() {
	}

	@Override
	public void run() {
		// NOTE: I know, it's a bummer we go over all this just to set status to
		// Timedout, but I think it's better to follow similar aproach for each
		// participant of the reconciliation workflow
		impl.setActionStatus(ActionStatus.Timedout);
	}

	public void setImpl(ActionExecutionSpiImpl impl) {
		this.impl = impl;
	}
}
