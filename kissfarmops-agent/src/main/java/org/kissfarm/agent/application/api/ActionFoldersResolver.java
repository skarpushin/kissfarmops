package org.kissfarm.agent.application.api;

import org.kissfarm.shared.config.dto.AppDefConfig;
import org.kissfarm.shared.config.dto.AppProtoConfig;

/**
 * Abstraction for resolving folders locations
 * 
 * @author Sergey Karpushin
 *
 */
public interface ActionFoldersResolver {
	String resolveInstanceFolder(AppDefConfig appDefConfig, AppProtoConfig appProtoConfig, String actionName);

	String resolveScriptsFolder(AppDefConfig appDefConfig, AppProtoConfig appProtoConfig, String actionName);
}
