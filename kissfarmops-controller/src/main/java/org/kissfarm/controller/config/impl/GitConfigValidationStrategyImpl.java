package org.kissfarm.controller.config.impl;

import org.kissfarm.controller.config.api.GitAbstraction;
import org.kissfarm.controller.config.dto.GitConfig;
import org.kissfarm.controller.config.errors.GitConfigValidationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.summerb.approaches.jdbccrud.impl.EasyCrudValidationStrategyAbstract;
import org.summerb.approaches.validation.ValidationContext;
import org.summerb.utils.exceptions.ExceptionUtils;

public class GitConfigValidationStrategyImpl extends EasyCrudValidationStrategyAbstract<GitConfig> {
	private Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	private GitAbstraction gitAbstraction;

	@Override
	protected void doValidateForCreate(GitConfig dto, ValidationContext ctx) {
		ctx.validateNotEmpty(dto.getUri(), GitConfig.FN_URI);
		ctx.validateNotEmpty(dto.getBranch(), GitConfig.FN_BRANCH);
		ctx.validateNotEmpty(dto.getUser(), GitConfig.FN_USER);
		ctx.validateNotEmpty(dto.getPassword(), GitConfig.FN_PASSWORD);
		if (!ctx.getHasErrors()) {
			try {
				gitAbstraction.assertGitConfig(dto);
			} catch (Throwable t) {
				log.warn("Git connection settings verification failed", t);
				ctx.add(new GitConfigValidationError(ExceptionUtils.getAllMessagesRaw(t)));
			}
		}
	}
}
