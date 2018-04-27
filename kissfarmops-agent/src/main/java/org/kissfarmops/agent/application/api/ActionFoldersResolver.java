package org.kissfarmops.agent.application.api;

import org.kissfarmops.shared.config.api.AppDefinitionConfig;
import org.kissfarmops.shared.config.api.AppPrototypeConfig;

/**
 * Abstraction for resolving folders locations
 * 
 * @author Sergey Karpushin
 *
 */
public interface ActionFoldersResolver {
	String resolveInstanceFolder(AppDefinitionConfig appDefinitionConfig, AppPrototypeConfig appPrototypeConfig,
			String actionName);

	String resolveScriptsFolder(AppDefinitionConfig appDefinitionConfig, AppPrototypeConfig appPrototypeConfig,
			String actionName);
}
