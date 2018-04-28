package org.kissfarm.controller.security.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.summerb.approaches.springmvc.security.apis.UserRegisteredHandler;
import org.summerb.approaches.springmvc.security.apis.UsersServiceFacade;
import org.summerb.microservices.users.api.dto.User;

public class UserRegisteredHandlerImpl implements UserRegisteredHandler {
	@Autowired
	private UsersServiceFacade usersServiceFacade;

	@Override
	public void onUserRegistered(User user) {
		try {
			usersServiceFacade.activateRegistration(user.getUuid());
		} catch (Throwable t) {
			throw new RuntimeException("Failed to perform user registration post actions", t);
		}
	}
}
