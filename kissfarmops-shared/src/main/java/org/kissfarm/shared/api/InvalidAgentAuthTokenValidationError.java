package org.kissfarm.shared.api;

import org.summerb.approaches.validation.ValidationError;

@SuppressWarnings("deprecation")
public class InvalidAgentAuthTokenValidationError extends ValidationError {
	private static final long serialVersionUID = 3000936905570083574L;

	public InvalidAgentAuthTokenValidationError() {
	}

	public InvalidAgentAuthTokenValidationError(String fieldToken, String reason) {
		super("invalid.agentAuthToken", fieldToken, reason);
	}
}
