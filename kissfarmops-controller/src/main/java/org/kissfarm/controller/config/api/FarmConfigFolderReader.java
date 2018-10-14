package org.kissfarm.controller.config.api;

import java.io.File;

import org.kissfarm.controller.config.dto.FarmConfig;

/**
 * Strategy to read FarmConfig from a folder
 * 
 * @author Sergey Karpushin
 *
 */
public interface FarmConfigFolderReader {

	FarmConfig readFarmConfig(File dir, String version);

	File buildAppDefDirByAppName(File farmConfigDir, String appDefName);

}