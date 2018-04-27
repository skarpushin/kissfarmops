package org.kissfarmops.agent.application.api;

import org.kissfarmops.shared.config.api.AppDefinitionConfig;
import org.kissfarmops.shared.config.api.AppPrototypeConfig;

public interface AppInstanceSpiFactory {

	AppInstanceSpi build(String version, AppDefinitionConfig definitionConfig, AppPrototypeConfig prototypeConfig,
			AppListener appListener);

}
