package org.kissfarm.agent.action_executor.impl_folder;

import org.kissfarm.agent.action_executor.api.ResumeMethod;
import org.kissfarm.shared.config.dto.ActionStatus;

public class StatusCheckTimedOutDeferedHandler implements ResumeMethod {
	private static final long serialVersionUID = 5137099904456434018L;

	private transient ActionExecutionSpiImpl impl;

	public StatusCheckTimedOutDeferedHandler() {
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
