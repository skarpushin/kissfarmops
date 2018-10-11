package org.kissfarm.agent.application.api;

import org.kissfarm.shared.config.dto.AppDefConfig;
import org.kissfarm.shared.config.dto.AppProtoConfig;

public interface AppInstanceSpiFactory {

	AppInstanceSpi build(String version, AppDefConfig definitionConfig, AppProtoConfig prototypeConfig,
			AppListener appListener);

}
