package org.kissfarm.agent.application.api;

import org.kissfarm.shared.config.dto.AppDefinitionConfig;
import org.kissfarm.shared.config.dto.AppPrototypeConfig;

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
