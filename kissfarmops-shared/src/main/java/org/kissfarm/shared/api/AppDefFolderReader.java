package org.kissfarm.shared.api;

import java.io.File;

import org.kissfarm.shared.config.dto.AppDefConfig;

public interface AppDefFolderReader {

	AppDefConfig readAppDefConfig(File appDir);

}
