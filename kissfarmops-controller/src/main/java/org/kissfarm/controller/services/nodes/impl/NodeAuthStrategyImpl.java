package org.kissfarm.controller.services.nodes.impl;

import java.util.HashMap;
import java.util.Map;

import org.kissfarm.controller.services.nodes.api.NodeAuthStrategy;
import org.kissfarm.controller.services.nodes.api.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.summerb.approaches.security.api.SecurityContextResolver;
import org.summerb.approaches.security.api.exceptions.NotAuthorizedException;
import org.summerb.approaches.springmvc.security.SecurityConstants;
import org.summerb.microservices.users.api.dto.User;

public class NodeAuthStrategyImpl implements NodeAuthStrategy {
	@Autowired
	protected SecurityContextResolver<User> securityContextResolver;

	protected User getUser() {
		return securityContextResolver.getUser();
	}

	@Override
	public void assertAuthorizedToCreate(Node dto) throws NotAuthorizedException {
		assertUserRole("action.create", "Node:" + dto.getHostName(), SecurityConstants.ROLE_BACKGROUND_PROCESS,
				SecurityConstants.ROLE_USER);
		// NOTE: Nodes are supposed to be self-registered. I'll leave User for now, for
		// dev purposes
	}

	@Override
	public void assertAuthorizedToRead(Node dto) throws NotAuthorizedException {
		assertUserRole("action.read", "Node:" + dto.getHostName(), SecurityConstants.ROLE_USER);
	}

	@Override
	public void assertAuthorizedToUpdate(Node existingVersion, Node newVersion) throws NotAuthorizedException {
		assertUserRole("action.update", "Node:" + existingVersion.getHostName(), SecurityConstants.ROLE_USER);

		assertFieldWasntChanged(existingVersion.getAgentAuthToken(), newVersion.getAgentAuthToken(),
				existingVersion.getId());
		assertFieldWasntChanged(existingVersion.getId(), newVersion.getId(), existingVersion.getId());
	}

	private void assertFieldWasntChanged(Object a, Object b, String id) throws NotAuthorizedException {
		if (!ObjectUtils.nullSafeEquals(a, b)) {
			throw new NotAuthorizedException(securityContextResolver.getUser().getDisplayName(),
					"action.modifyImmutableFields", id);
		}
	}

	@Override
	public void assertAuthorizedToDelete(Node dto) throws NotAuthorizedException {
		assertUserRole("action.delete", "Node:" + dto.getHostName(), SecurityConstants.ROLE_USER);
	}

	protected void assertUserRole(String operationMessageCode, String NodeTag, String... role)
			throws NotAuthorizedException {
		if (!securityContextResolver.hasAnyRole(role)) {
			throw new NotAuthorizedException(securityContextResolver.getUser().getDisplayName(), operationMessageCode,
					NodeTag);
		}
	}

	@Override
	public Map<String, Boolean> resolvePermissions() {
		boolean isUser = securityContextResolver.hasRole(SecurityConstants.ROLE_USER);
		Map<String, Boolean> ret = new HashMap<>();
		ret.put("create", true); // TODO: Change it to false
		ret.put("read", isUser);
		ret.put("update", isUser);
		ret.put("delete", isUser);
		return ret;
	}
}
