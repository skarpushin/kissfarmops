package org.kissfarm.controller.config.api;

import java.util.Set;

import org.kissfarm.controller.config.dto.FarmConfig;

public interface FarmConfigPackager {

	/**
	 * Prepare config package for the tags
	 * 
	 * @param farmConfig     Farm Config
	 * @param workTreeFolder folder on a file system where a corresponding Farm
	 *                       Config is located
	 * @param nodeTags       node tags to use to identify applicable applications
	 * @return path name of the package file on a file system
	 */
	String preparePackage(FarmConfig farmConfig, String workTreeFolder, String nodeTags);

	String preparePackage(FarmConfig farmConfig, String workTreeFolder, Set<String> nodeTags);

	String getPackageFullPathByBaseName(String packageId);

}
