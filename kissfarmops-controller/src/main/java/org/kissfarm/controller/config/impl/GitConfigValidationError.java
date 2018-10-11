package org.kissfarm.controller.config.impl;

import org.kissfarm.controller.config.api.GitConfig;
import org.summerb.approaches.validation.ValidationError;

public class GitConfigValidationError extends ValidationError {
	private static final long serialVersionUID = 5351282707368340684L;
	private String[] args;

	@SuppressWarnings("deprecation")
	public GitConfigValidationError(String details) {
		super("validation.invalidGitConfig", GitConfig.FN_URI);
		args = new String[] { details };
	}

	@SuppressWarnings("deprecation")
	public GitConfigValidationError() {
	}

	@Override
	public Object[] getMessageArgs() {
		return args;
	}
}
