package org.kissfarm.controller.security.impl;

import org.summerb.approaches.springmvc.security.UsersServiceFacadeImpl;
import org.summerb.approaches.springmvc.security.dto.Registration;
import org.summerb.approaches.springmvc.security.dto.UserStatus;
import org.summerb.approaches.validation.FieldValidationException;

public class UsersServiceFacadeExImpl extends UsersServiceFacadeImpl {
	@Override
	protected void validateRegistration(Registration registration) throws FieldValidationException {
		super.validateRegistration(registration);

		UserStatus userStatus = getUserStatusByEmail(registration.getEmail());
		if (userStatus == UserStatus.NotExists) {
			throw new FieldValidationException(new OnlyProvisionedUsersAllowedValidationError());
		}
	}
}
