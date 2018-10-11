package org.kissfarm.controller.config.impl;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.kissfarm.controller.config.api.GitAbstraction;
import org.kissfarm.controller.config.api.GitConfig;
import org.kissfarm.controller.config.api.RemoteConfigRepoNotAvailableException;
import org.kissfarm.controller.config.api.RemoteConfigRepoNotConfiguredException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.ProfileValueSourceConfiguration;
import org.springframework.test.annotation.SystemProfileValueSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * This test is used only to test communication with remote repos which requires
 * authentication.
 * 
 * All tests cases which tests ConfigProvider logic is placed in other test
 * suite that works with bundles from src/test/resources
 * 
 * @author Sergey Karpushin
 *
 */
@ContextConfiguration("classpath:git-test-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
@ProfileValueSourceConfiguration(SystemProfileValueSource.class)
public class ConfigProviderGitImplRemoteTest {
	@Autowired
	private GitAbstraction gitAbstraction;
	@Autowired
	private GitConfig gitConfig;

	@Test
	public void testCanConnectToRemoteRepoSecuredByPassword()
			throws RemoteConfigRepoNotConfiguredException, RemoteConfigRepoNotAvailableException {
		String result = gitAbstraction.getRemoteVersion(gitConfig);
		assertNotNull(result);
	}
}
