package org.kissfarm.controller.config.api;

import java.io.File;

/**
 * Strategy to read FarmConfig from a folder
 * 
 * @author Sergey Karpushin
 *
 */
public interface FarmConfigFolderReader {

	FarmConfig readFarmConfig(File dir, String version);

}