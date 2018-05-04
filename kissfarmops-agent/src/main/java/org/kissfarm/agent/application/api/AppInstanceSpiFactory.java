package org.kissfarm.agent.application.api;

import org.kissfarm.shared.config.dto.AppDefinitionConfig;
import org.kissfarm.shared.config.dto.AppPrototypeConfig;

public interface AppInstanceSpiFactory {

	AppInstanceSpi build(String version, AppDefinitionConfig definitionConfig, AppPrototypeConfig prototypeConfig,
			AppListener appListener);

}
