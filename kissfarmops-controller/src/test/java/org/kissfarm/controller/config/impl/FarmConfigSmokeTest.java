package org.kissfarm.controller.config.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kissfarm.controller.config.api.FarmConfig;
import org.kissfarm.controller.config.api.FarmConfigFolderReader;
import org.kissfarm.controller.config.api.GitAbstraction;
import org.kissfarm.controller.config.api.GitConfig;
import org.kissfarm.shared.api.ZipUtils;
import org.kissfarm.shared.impl.ZipUtilsImpl;

import com.google.common.io.Files;

public class FarmConfigSmokeTest {
	private List<File> filesToBeDeleted = new LinkedList<>();
	private ZipUtils zipUtils = new ZipUtilsImpl();

	@Before
	public void beforeEachTest() {
	}

	@After
	public void afterEachTest() throws Exception {
		while (filesToBeDeleted.size() > 0) {
			FileUtils.deleteQuietly(filesToBeDeleted.remove(0));
		}
	}

	@Test
	public void expectNormalResult() throws Exception {
		File dir = unpackTestPackageToTempDir("repoA-v1.zip");
		GitAbstraction f = buildFixture(dir);
		String version = f.cloneRepo(buildGitConfig(dir), createTempDir().getAbsolutePath());
		FarmConfig result = buildReader().readFarmConfig(dir, version);
		assertNotNull(result);
	}

	private FarmConfigFolderReader buildReader() {
		return new FarmConfigFolderReaderImpl();
	}

	@Test
	public void expectNormalResultWithExtraAction() throws Exception {
		File dir = unpackTestPackageToTempDir("repoA-v2-added actionA.zip");
		GitAbstraction f = buildFixture(dir);
		String version = f.cloneRepo(buildGitConfig(dir), createTempDir().getAbsolutePath());
		FarmConfig result = buildReader().readFarmConfig(dir, version);
		assertNotNull(result);
	}

	@Test(expected = RuntimeException.class)
	public void expectExcOnInvalidTags() throws Exception {
		File dir = unpackTestPackageToTempDir("repoA-v2-inv-tags.zip");
		GitAbstraction f = buildFixture(dir);
		String version = f.pullChanges(buildGitConfig(dir), createTempDir().getAbsolutePath());
		FarmConfig result = buildReader().readFarmConfig(dir, version);
	}

	@Test
	public void expectOkWhenActionAdded() throws Exception {
		File dir = unpackTestPackageToTempDir("repoA-v1.zip");
		GitAbstraction f = buildFixture(dir);
		String workDir = createTempDir().getAbsolutePath();
		String version = f.cloneRepo(buildGitConfig(dir), workDir);
		FarmConfig result = buildReader().readFarmConfig(dir, version);
		assertNotNull(result);
		assertEquals(1, result.getAppDefs().get("mint183").getActions().size());
		assertNotNull(result.getAppDefs().get("mint183").getActions().get("getStatus"));

		FileUtils.forceDelete(dir);
		unpuckTestPackageToDir("repoA-v2-added actionA.zip", dir);
		version = f.pullChanges(buildGitConfig(dir), workDir);
		FarmConfig result2 = buildReader().readFarmConfig(dir, version);
		assertNotNull(result2);
		assertEquals(2, result2.getAppDefs().get("mint183").getActions().size());
		assertNotNull(result2.getAppDefs().get("mint183").getActions().get("actionA"));
		FarmConfigTools.assertFarmConfigChangeValid(result, result2);
	}

	@Test
	public void expectOkOnParams2() throws Exception {
		File dir = unpackTestPackageToTempDir("repoA-v3-inv param change.zip");
		GitAbstraction f = buildFixture(dir);
		String version = f.cloneRepo(buildGitConfig(dir), createTempDir().getAbsolutePath());
		FarmConfig result = buildReader().readFarmConfig(dir, version);
	}

	@Test(expected = RuntimeException.class)
	public void expectExcWhenActionParamsChanged() throws Exception {
		File dir = unpackTestPackageToTempDir("repoA-v2-added actionA.zip");
		GitAbstraction f = buildFixture(dir);
		String workDir = createTempDir().getAbsolutePath();
		String version = f.cloneRepo(buildGitConfig(dir), workDir);
		FarmConfig result = buildReader().readFarmConfig(dir, version);

		FileUtils.forceDelete(dir);
		unpuckTestPackageToDir("repoA-v3-inv param change.zip", dir);
		version = f.pullChanges(buildGitConfig(dir), workDir);
		FarmConfig result2 = buildReader().readFarmConfig(dir, version);
		FarmConfigTools.assertFarmConfigChangeValid(result, result2);
	}

	@Test
	public void expectNormalWithStatus2() throws Exception {
		File dir = unpackTestPackageToTempDir("repoA-v2-inv-status-type-change.zip");
		GitAbstraction f = buildFixture(dir);
		String version = f.cloneRepo(buildGitConfig(dir), createTempDir().getAbsolutePath());
		FarmConfig result = buildReader().readFarmConfig(dir, version);
		assertNotNull(result);
	}

	@Test(expected = RuntimeException.class)
	public void expectExcWhenIncompatibleStatusTypeChange() throws Exception {
		File dir = unpackTestPackageToTempDir("repoA-v1.zip");
		GitAbstraction f = buildFixture(dir);
		String workDir = createTempDir().getAbsolutePath();
		String version = f.cloneRepo(buildGitConfig(dir), workDir);
		FarmConfig result = buildReader().readFarmConfig(dir, version);

		FileUtils.forceDelete(dir);
		unpuckTestPackageToDir("repoA-v2-inv-status-type-change.zip", dir);
		version = f.pullChanges(buildGitConfig(dir), workDir);
		FarmConfig result2 = buildReader().readFarmConfig(dir, version);
		FarmConfigTools.assertFarmConfigChangeValid(result, result2);
	}

	private GitAbstraction buildFixture(File remoteDir) throws Exception {
		return new GitAbstractionImpl();
	}

	private GitConfig buildGitConfig(File remoteDir) {
		GitConfig gitConfig = new GitConfig();
		gitConfig.setBranch("master");
		gitConfig.setUri(remoteDir.getAbsolutePath());
		gitConfig.setUser("local");
		gitConfig.setPassword("local");
		return gitConfig;
	}

	private File unpackTestPackageToTempDir(String testPackageFileName) throws URISyntaxException {
		File tempDir = createTempDir();
		unpuckTestPackageToDir(testPackageFileName, tempDir);
		return tempDir;
	}

	private void unpuckTestPackageToDir(String testPackageFileName, File tempDir) throws URISyntaxException {
		URL resource = getClass().getClassLoader().getResource(testPackageFileName);
		File zipFile = new File(resource.toURI());
		zipUtils.unzip(zipFile, tempDir);
	}

	private File createTempDir() {
		File tempDir = Files.createTempDir();
		filesToBeDeleted.add(tempDir);
		return tempDir;
	}
}
