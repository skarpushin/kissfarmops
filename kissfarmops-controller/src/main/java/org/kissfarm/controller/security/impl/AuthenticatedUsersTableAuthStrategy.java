package org.kissfarm.controller.security.impl;

import java.util.HashMap;
import java.util.Map;

import org.kissfarm.controller.security.SecurityConstantsEx;
import org.springframework.beans.factory.annotation.Autowired;
import org.summerb.approaches.jdbccrud.rest.permissions.Permissions;
import org.summerb.approaches.jdbccrud.rest.permissions.PermissionsResolverPerTable;
import org.summerb.approaches.security.api.SecurityContextResolver;
import org.summerb.approaches.security.api.exceptions.NotAuthorizedException;
import org.summerb.approaches.springmvc.security.SecurityConstants;
import org.summerb.microservices.users.api.dto.User;

public class AuthenticatedUsersTableAuthStrategy extends EasyCrudTableAuthStrategyAbstract
		implements PermissionsResolverPerTable {
	@Autowired
	protected SecurityContextResolver<User> securityContextResolver;

	// NOTE: At this moment we allow all authenticated users to manipulate this
	// data. But it doens't feel right. Later on I should define restrictions
	// better.
	private String[] allowedRoles = new String[] { SecurityConstantsEx.ROLE_NODE, SecurityConstantsEx.ROLE_USER,
			SecurityConstantsEx.ROLE_ADMIN, SecurityConstantsEx.ROLE_BACKGROUND_PROCESS };

	@Override
	public void assertAuthorizedToRead() throws NotAuthorizedException {
		assertUserRole(Permissions.READ, allowedRoles);
	}

	@Override
	protected void assertAuthorizedToModify() throws NotAuthorizedException {
		assertUserRole(Permissions.UPDATE, allowedRoles);
	}

	@Override
	public Map<String, Boolean> resolvePermissions() {
		boolean isUser = securityContextResolver.hasRole(SecurityConstants.ROLE_USER);
		Map<String, Boolean> ret = new HashMap<>();
		ret.put("create", isUser);
		ret.put("read", isUser);
		ret.put("update", isUser);
		ret.put("delete", isUser);
		return ret;
	}
}
