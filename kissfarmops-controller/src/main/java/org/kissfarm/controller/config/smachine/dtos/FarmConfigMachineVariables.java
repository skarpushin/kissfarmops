package org.kissfarm.controller.config.smachine.dtos;

import org.kissfarm.controller.config.api.GitConfig;
import org.summerb.approaches.jdbccrud.common.DtoBase;

public class FarmConfigMachineVariables implements DtoBase {
	private static final long serialVersionUID = 593137241411508256L;

	public static final String MACHINE_TYPE = "FarmConfig";

	// TODO: Security concern. This data will be stored as-is in the database. Given
	// the actual intended use it shouldn't be a huge problem, but still it's a
	// legitimate security concern
	private GitConfig gitConfig;

	/**
	 * Folder temporarily used to get new config and validate it
	 */
	private String stagingWorkTree;
	private String stagingVersion;

	private String activeVersion;
	private String activeWorkTree;

	public String getActiveVersion() {
		return activeVersion;
	}

	public void setActiveVersion(String lastKnownGoodConfigVersion) {
		this.activeVersion = lastKnownGoodConfigVersion;
	}

	public GitConfig getGitConfig() {
		return gitConfig;
	}

	public void setGitConfig(GitConfig gitConnectionInfo) {
		this.gitConfig = gitConnectionInfo;
	}

	public String getStagingWorkTree() {
		return stagingWorkTree;
	}

	public void setStagingWorkTree(String stagingWorkTree) {
		this.stagingWorkTree = stagingWorkTree;
	}

	public String getActiveWorkTree() {
		return activeWorkTree;
	}

	public void setActiveWorkTree(String activeWorkTree) {
		this.activeWorkTree = activeWorkTree;
	}

	public String getStagingVersion() {
		return stagingVersion;
	}

	public void setStagingVersion(String stagingVersion) {
		this.stagingVersion = stagingVersion;
	}
}
