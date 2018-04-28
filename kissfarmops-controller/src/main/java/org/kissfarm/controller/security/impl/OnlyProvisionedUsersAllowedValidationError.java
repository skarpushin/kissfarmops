package org.kissfarm.controller.security.impl;

import org.summerb.approaches.validation.ValidationError;
import org.summerb.microservices.users.api.dto.User;

public class OnlyProvisionedUsersAllowedValidationError extends ValidationError {
	private static final long serialVersionUID = 5313005283402773533L;

	@SuppressWarnings("deprecation")
	public OnlyProvisionedUsersAllowedValidationError() {
		super("error.onyProvisionedUsersAllowed", User.FN_EMAIL);
	}
}
