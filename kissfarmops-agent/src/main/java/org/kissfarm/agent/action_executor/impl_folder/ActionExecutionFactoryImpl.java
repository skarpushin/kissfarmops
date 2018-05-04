package org.kissfarm.agent.action_executor.impl_folder;

import java.util.concurrent.ScheduledExecutorService;

import org.kissfarm.agent.action_executor.api.ActionExecutionFactory;
import org.kissfarm.agent.action_executor.api.ActionExecutionSpi;
import org.kissfarm.agent.action_executor.api.ActionsExecutionListener;
import org.kissfarm.agent.process_execution.api.ProcessExecutorFactory;
import org.kissfarm.agent.serializer.api.DtoSerializer;

import com.google.common.base.Preconditions;

public class ActionExecutionFactoryImpl implements ActionExecutionFactory {
	private DtoSerializer dtoSerializer;
	private ScheduledExecutorService executorService;
	private ProcessExecutorFactory processExecutorFactory;

	public ActionExecutionFactoryImpl(DtoSerializer dtoSerializer, ScheduledExecutorService executorService,
			ProcessExecutorFactory processExecutorFactory) {
		Preconditions.checkArgument(dtoSerializer != null, "dtoSerializer required");
		Preconditions.checkArgument(executorService != null, "executorService required");
		Preconditions.checkArgument(processExecutorFactory != null, "processExecutorFactory required");

		this.dtoSerializer = dtoSerializer;
		this.executorService = executorService;
		this.processExecutorFactory = processExecutorFactory;
	}

	@Override
	public ActionExecutionSpi startNewInvocation(ActionInvocationInfo invocationInfo,
			ActionsExecutionListener actionsExecutionListener) {
		return ActionExecutionSpiImpl.startNewInvocation(invocationInfo, dtoSerializer, executorService,
				processExecutorFactory, actionsExecutionListener);
	}

	@Override
	public ActionExecutionSpi postponeAction(ActionInvocationInfo invocationInfo,
			ActionsExecutionListener actionsExecutionListener) {
		return ActionExecutionSpiImpl.postponeAction(invocationInfo, dtoSerializer, actionsExecutionListener);
	}

	@Override
	public ActionExecutionSpi reconcileExistingAction(String instanceFolder,
			ActionsExecutionListener actionsExecutionListener) {
		return ActionExecutionSpiImpl.reconcileExistingAction(instanceFolder, dtoSerializer, executorService,
				processExecutorFactory, actionsExecutionListener);
	}

}
