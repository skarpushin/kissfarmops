package org.kissfarm.controller.config.impl;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LsRemoteCommand;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.TransportCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.kissfarm.controller.config.api.GitAbstraction;
import org.kissfarm.controller.config.dto.GitConfig;
import org.kissfarm.controller.config.errors.RemoteConfigRepoNotAvailableException;
import org.kissfarm.controller.config.errors.RemoteConfigRepoNotConfiguredException;
import org.kissfarm.shared.tools.Defaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.google.common.base.Preconditions;

/**
 * NOTE: JGit cookbook: https://github.com/centic9/jgit-cookbook
 */
public class GitAbstractionImpl implements GitAbstraction {
	private static Logger log = LoggerFactory.getLogger(GitAbstractionImpl.class);

	private int hashSize = 7;

	@Override
	public void assertGitConfig(GitConfig dto) throws Exception {
		String version = getRemoteVersion(dto);
		Preconditions.checkArgument(StringUtils.hasText(version), "GitConfig doesnt lead to valid repo");
	}

	@Override
	public String getRemoteVersion(GitConfig gitConfig)
			throws RemoteConfigRepoNotConfiguredException, RemoteConfigRepoNotAvailableException {
		if (gitConfig == null || !StringUtils.hasText(gitConfig.getUri())) {
			throw new RemoteConfigRepoNotConfiguredException();
		}

		try {
			// NOTE: I didn't find way to set URI, but it looks like if you set url as a
			// remote it wokrs. It doesn't feel right though because normally remote is
			// something like "origin" string literlal. Not url.
			LsRemoteCommand cmd = Git.lsRemoteRepository().setRemote(gitConfig.getUri()).setTimeout(30).setHeads(true);
			initRemoteCommand(cmd, gitConfig);
			Map<String, Ref> refs = cmd.callAsMap();
			Ref ref = refs.get("refs/heads/" + gitConfig.getBranch());
			return getVersion(ref);
		} catch (Throwable t) {
			log.warn("Failed to getRemoteVersion", t);
			throw new RemoteConfigRepoNotAvailableException(t);
		}
	}

	@Override
	public String getLocalVersion(String workTreeFolder) {
		Git git = null;
		try {
			git = openLocalRepo(workTreeFolder);
			String localVersion = getVersion(git);
			Preconditions.checkArgument(localVersion != null, "Couldn't identify current version");
			git.close();

			return localVersion;
		} catch (Throwable t) {
			log.warn("Failed to getRemoteVersion", t);
			throw new RuntimeException("Failed to getLocalVersion", t);
		} finally {
			if (git != null) {
				git.close();
			}
		}
	}

	private Git openLocalRepo(String workTreeFolder) throws IOException {
		File workTreeFolderFile = new File(workTreeFolder);
		boolean gitSubFolderExists = workTreeFolderFile.exists()
				&& new File(workTreeFolderFile.getAbsolutePath() + File.separator + ".git").exists();
		Preconditions.checkArgument(gitSubFolderExists, ".git subfolder wasn't found in %s", workTreeFolder);

		Git git = Git.open(workTreeFolderFile);
		return git;
	}

	@Override
	public String cloneRepo(GitConfig gitConfig, String workTreeFolder)
			throws RemoteConfigRepoNotConfiguredException, RemoteConfigRepoNotAvailableException {
		Git git = null;
		try {
			File workTreeFolderFile = prepareWorkTreeFolder(workTreeFolder);

			log.debug("Clonning repo from: " + gitConfig.getUri());
			CloneCommand cmd = Git.cloneRepository().setTimeout(30);
			initRemoteCommand(cmd, gitConfig);
			cmd.setURI(gitConfig.getUri());
			cmd.setBranch(gitConfig.getBranch());
			cmd.setDirectory(workTreeFolderFile);
			git = cmd.call();
			log.debug("Repo clonned to: " + workTreeFolder);
			return getVersion(git);
		} catch (Throwable t) {
			throw new RuntimeException("Failed to clone repo: " + gitConfig.getUri(), t);
		} finally {
			if (git != null) {
				git.close();
			}
		}
	}

	private File prepareWorkTreeFolder(String workTreeFolder) throws IOException {
		File workTreeFolderFile = new File(workTreeFolder);
		if (workTreeFolderFile.exists()) {
			log.debug("Cleaning dir {} before clonining", workTreeFolderFile);
			FileUtils.deleteDirectory(workTreeFolderFile);
		}
		Preconditions.checkState(workTreeFolderFile.mkdirs(), "Failed to create worktree dir %s", workTreeFolderFile);
		Preconditions.checkState(isCanWriteToFolder(workTreeFolderFile), "Can't write to work tree folder %s",
				workTreeFolderFile);
		return workTreeFolderFile;
	}

	@Override
	public String pullChanges(GitConfig gitConfig, String workTreeFolder) {
		Git git = null;
		try {
			git = openLocalRepo(workTreeFolder);
			String localVersion = getVersion(git);
			String remoteVersion = getRemoteVersion(gitConfig);
			if (remoteVersion.equals(localVersion)) {
				return null;
			}

			PullCommand cmd = git.pull();
			initRemoteCommand(cmd, gitConfig);
			PullResult pullResult = cmd.call();
			Preconditions.checkState(pullResult.isSuccessful(), "Git pull failed: %s", pullResult.toString());

			return remoteVersion;
		} catch (Throwable t) {
			log.warn("Failed to getRemoteVersion", t);
			throw new RuntimeException("Failed to getLocalVersion", t);
		} finally {
			if (git != null) {
				git.close();
			}
		}
	}

	private String getVersion(Git fromGit) throws GitAPIException, IOException {
		String refName = fromGit.getRepository().getFullBranch();
		List<Ref> branches = fromGit.branchList().call();
		Ref ref = branches.stream().filter(x -> refName.equals(x.getName())).findFirst()
				.orElseThrow(() -> new IllegalStateException("Branch " + refName + " wasn't found in repo"));
		return getVersion(ref);
	}

	private String getVersion(Ref ref) {
		return ref.getObjectId().getName().substring(0, hashSize);
	}

	private void initRemoteCommand(TransportCommand<?, ?> setHeads, GitConfig connectionInfo) {
		setHeads.setCredentialsProvider(
				new UsernamePasswordCredentialsProvider(connectionInfo.getUser(), connectionInfo.getPassword()));
	}

	public static boolean isCanWriteToFolder(File localRepo) {
		try {
			File testFile = new File(localRepo.getAbsolutePath() + File.separator + "test");
			if (testFile.exists()) {
				FileUtils.forceDelete(testFile);
			} else {
				FileUtils.writeStringToFile(testFile, "test", Defaults.ENCODING);
				FileUtils.forceDelete(testFile);
			}
			return true;
		} catch (Throwable t) {
			log.error("Failed to ensure folder is writable", t);
			return false;
		}
	}

}
