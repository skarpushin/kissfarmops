package org.kissfarm.agent.action_executor.impl_folder;

import java.util.Map;

import org.kissfarm.agent.action_executor.api.ActionStatus;

public class StartPostponedActionDeferedHandler implements ResumeMethod {
	private static final long serialVersionUID = -3268509627747786241L;

	private ActionExecutionSpiImpl impl;

	public StartPostponedActionDeferedHandler() {
	}

	@Override
	public void run() {
		try {
			// not set status and spawn a process
			impl.setActionStatus(ActionStatus.InProgressSync);

			Map<String, String> enrichedEnvVars = impl.enrichEnvVarsBeforeScriptExecution();
			impl.invokeAction(impl.invocationInfo.getScriptsFolder(), impl.invocationInfo.getActionCommands(),
					impl.executorService, impl.processExecutorFactory, enrichedEnvVars);
		} catch (Throwable t) {
			impl.logExceptionToBoth("Exception happened while invoking postponed action " + impl.toStringForLog(), t);
			impl.setActionStatus(ActionStatus.Exception);
		}
	}

	@Override
	public void setImpl(ActionExecutionSpiImpl impl) {
		this.impl = impl;
	}

}
