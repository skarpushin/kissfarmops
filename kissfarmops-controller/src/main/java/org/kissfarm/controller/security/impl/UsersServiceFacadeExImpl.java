package org.kissfarm.controller.security.impl;

import java.util.List;

import org.kissfarm.controller.security.SecurityConstantsEx;
import org.summerb.approaches.springmvc.security.SecurityConstants;
import org.summerb.approaches.springmvc.security.UsersServiceFacadeImpl;
import org.summerb.approaches.springmvc.security.dto.Registration;
import org.summerb.approaches.validation.FieldValidationException;
import org.summerb.microservices.users.api.dto.User;
import org.summerb.microservices.users.api.exceptions.UserNotFoundException;

public class UsersServiceFacadeExImpl extends UsersServiceFacadeImpl {
	@Override
	protected void validateRegistration(Registration registration) throws FieldValidationException {
		super.validateRegistration(registration);

		try {
			User user = getUserService().getUserByEmail(registration.getEmail());

			List<String> permissions = getPermissionService().findUserPermissionsForSubject(SecurityConstants.DOMAIN,
					user.getUuid(), null);

			if (permissions.contains(SecurityConstantsEx.ROLE_NODE)) {
				throw new RuntimeException("ROLE_NODE is not supposed to be registered as a normal user");
			}
			if (permissions.contains(SecurityConstantsEx.ROLE_BACKGROUND_PROCESS)) {
				throw new RuntimeException("ROLE_BACKGROUND_PROCESS is not supposed to be registered as a normal user");
			}
		} catch (UserNotFoundException e) {
			throw new FieldValidationException(new OnlyProvisionedUsersAllowedValidationError());
		}
	}

	@Override
	public void validateUserAllowedToLogin(String username) throws FieldValidationException {
		User user;
		try {
			user = getUserService().getUserByEmail(username);
		} catch (UserNotFoundException e) {
			// this case will be handled later, don't care
			throw new RuntimeException("Can't login user which doesn't exist: " + username, e);
		}

		List<String> permissions = getPermissionService().findUserPermissionsForSubject(SecurityConstants.DOMAIN,
				user.getUuid(), null);

		if (permissions.contains(SecurityConstantsEx.ROLE_BACKGROUND_PROCESS)) {
			throw new RuntimeException("Background process is not supposed to perform interactive login");
		}

		// NOTE: We allow for any other user
	}
}
