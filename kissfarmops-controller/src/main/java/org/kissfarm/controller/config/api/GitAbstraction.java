package org.kissfarm.controller.config.api;

import org.kissfarm.controller.config.dto.GitConfig;
import org.kissfarm.controller.config.errors.RemoteConfigRepoNotAvailableException;
import org.kissfarm.controller.config.errors.RemoteConfigRepoNotConfiguredException;

/**
 * Simple abstraction layer for GIT operations. Super simple interface that will
 * simplify/abstract GIT usage
 * 
 * @author sergeyk
 *
 */
public interface GitAbstraction {

	/**
	 * Check if given config will lead to a repo
	 * 
	 * @exception Exception in case assertion failed
	 */
	void assertGitConfig(GitConfig dto) throws Exception;

	/**
	 * @return hash of the latest commit in a given branch/repo
	 */
	String getRemoteVersion(GitConfig gitConfig)
			throws RemoteConfigRepoNotConfiguredException, RemoteConfigRepoNotAvailableException;

	/**
	 * @return Get latest hash for a local git-based folder
	 */
	String getLocalVersion(String workTreeFolder);

	/**
	 * Clone repo
	 * 
	 * @return latest hash
	 */
	String cloneRepo(GitConfig gitConfig, String workTreeFolder)
			throws RemoteConfigRepoNotConfiguredException, RemoteConfigRepoNotAvailableException;

	/**
	 * Pull changes from remote repo
	 * 
	 * @return new version hash -OR- null if no changes detected
	 */
	String pullChanges(GitConfig gitConfig, String workTreeFolder);

}
