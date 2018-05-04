package org.kissfarm.agent.application.impl;

import org.kissfarm.agent.action_executor.api.ActionExecutionFactory;
import org.kissfarm.agent.application.api.ActionFoldersResolver;
import org.kissfarm.agent.application.api.AppInstanceSpi;
import org.kissfarm.agent.application.api.AppInstanceSpiFactory;
import org.kissfarm.agent.application.api.AppListener;
import org.kissfarmops.shared.config.api.AppDefinitionConfig;
import org.kissfarmops.shared.config.api.AppPrototypeConfig;

public class AppInstanceSpiFactoryImpl implements AppInstanceSpiFactory {
	private ActionFoldersResolver actionFoldersResolver;
	private ActionExecutionFactory actionExecutionFactory;

	public AppInstanceSpiFactoryImpl(ActionFoldersResolver actionFoldersResolver,
			ActionExecutionFactory actionExecutionFactory) {
		super();
		this.actionFoldersResolver = actionFoldersResolver;
		this.actionExecutionFactory = actionExecutionFactory;
	}

	@Override
	public AppInstanceSpi build(String version, AppDefinitionConfig definitionConfig,
			AppPrototypeConfig prototypeConfig, AppListener appListener) {

		return new AppInstanceSpiImpl(version, definitionConfig, prototypeConfig, appListener, actionFoldersResolver,
				actionExecutionFactory);
	}
}
