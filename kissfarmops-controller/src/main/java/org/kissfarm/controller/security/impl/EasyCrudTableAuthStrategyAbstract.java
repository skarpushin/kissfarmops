package org.kissfarm.controller.security.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.summerb.approaches.jdbccrud.api.EasyCrudTableAuthStrategy;
import org.summerb.approaches.security.api.SecurityContextResolver;
import org.summerb.approaches.security.api.exceptions.NotAuthorizedException;
import org.summerb.microservices.users.api.PermissionService;
import org.summerb.microservices.users.api.dto.User;

public abstract class EasyCrudTableAuthStrategyAbstract implements EasyCrudTableAuthStrategy {
	@Autowired
	protected SecurityContextResolver<User> securityContextResolver;
	@Autowired
	protected PermissionService permissionService;

	@Override
	public void assertAuthorizedToCreate() throws NotAuthorizedException {
		assertAuthorizedToModify();
	}

	@Override
	public void assertAuthorizedToUpdate() throws NotAuthorizedException {
		assertAuthorizedToModify();
	}

	protected abstract void assertAuthorizedToModify() throws NotAuthorizedException;

	@Override
	public void assertAuthorizedToDelete() throws NotAuthorizedException {
		assertAuthorizedToModify();
	}

	protected void assertUserRole(String operationMessageCode, String... role) throws NotAuthorizedException {
		if (!securityContextResolver.hasAnyRole(role)) {
			throw new NotAuthorizedException(securityContextResolver.getUser().getDisplayName(), operationMessageCode);
		}
	}
}
