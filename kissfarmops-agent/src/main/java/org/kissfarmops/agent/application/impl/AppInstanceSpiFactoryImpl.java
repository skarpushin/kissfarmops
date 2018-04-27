package org.kissfarmops.agent.application.impl;

import java.util.concurrent.ScheduledExecutorService;

import org.kissfarmops.agent.application.api.ActionFoldersResolver;
import org.kissfarmops.agent.application.api.AppInstanceSpi;
import org.kissfarmops.agent.application.api.AppInstanceSpiFactory;
import org.kissfarmops.agent.application.api.AppListener;
import org.kissfarmops.agent.process_execution.api.ProcessExecutorFactory;
import org.kissfarmops.agent.serializer.api.DtoSerializer;
import org.kissfarmops.shared.config.api.AppDefinitionConfig;
import org.kissfarmops.shared.config.api.AppPrototypeConfig;

public class AppInstanceSpiFactoryImpl implements AppInstanceSpiFactory {
	private ScheduledExecutorService executorService;
	private ActionFoldersResolver actionFoldersResolver;
	private DtoSerializer dtoSerializer;
	private ProcessExecutorFactory processExecutorFactory;

	public AppInstanceSpiFactoryImpl(ActionFoldersResolver actionFoldersResolver,
			ScheduledExecutorService executorService, DtoSerializer dtoSerializer,
			ProcessExecutorFactory processExecutorFactory) {
		super();
		this.actionFoldersResolver = actionFoldersResolver;
		this.executorService = executorService;
		this.dtoSerializer = dtoSerializer;
		this.processExecutorFactory = processExecutorFactory;
	}

	@Override
	public AppInstanceSpi build(String version, AppDefinitionConfig definitionConfig,
			AppPrototypeConfig prototypeConfig, AppListener appListener) {

		return new AppInstanceSpiImpl(version, definitionConfig, prototypeConfig, appListener,
				actionFoldersResolver, executorService, processExecutorFactory, dtoSerializer);
	}
}
