package org.kissfarm.controller.services.nodes.impl;

import java.util.HashMap;
import java.util.Map;

import org.kissfarm.controller.security.SecurityConstantsEx;
import org.kissfarm.controller.services.nodes.api.NodeTag;
import org.kissfarm.controller.services.nodes.api.NodeTagAuthStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.summerb.approaches.security.api.SecurityContextResolver;
import org.summerb.approaches.security.api.exceptions.NotAuthorizedException;
import org.summerb.approaches.springmvc.security.SecurityConstants;
import org.summerb.microservices.users.api.dto.User;

public class NodeTagAuthStrategyImpl implements NodeTagAuthStrategy {
	@Autowired
	protected SecurityContextResolver<User> securityContextResolver;

	protected User getUser() {
		return securityContextResolver.getUser();
	}

	@Override
	public void assertAuthorizedToCreate(NodeTag dto) throws NotAuthorizedException {
		assertUserRole("action.create", "NodeTag:" + getEntityName(dto), SecurityConstants.ROLE_USER,
				SecurityConstants.ROLE_BACKGROUND_PROCESS);
	}

	private String getEntityName(NodeTag dto) {
		return dto.getSubjectId() + "=" + dto.getTag();
	}

	@Override
	public void assertAuthorizedToRead(NodeTag dto) throws NotAuthorizedException {
		assertUserRole("action.read", "NodeTag:" + getEntityName(dto), SecurityConstants.ROLE_USER,
				SecurityConstantsEx.ROLE_NODE);
	}

	@Override
	public void assertAuthorizedToUpdate(NodeTag existingVersion, NodeTag newVersion) throws NotAuthorizedException {
		assertUserRole("action.update", "NodeTag:" + getEntityName(existingVersion), SecurityConstants.ROLE_USER);
	}

	@Override
	public void assertAuthorizedToDelete(NodeTag dto) throws NotAuthorizedException {
		assertUserRole("action.delete", "NodeTag:" + getEntityName(dto), SecurityConstants.ROLE_USER);
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
		ret.put("create", isUser);
		ret.put("read", isUser);
		ret.put("update", isUser);
		ret.put("delete", isUser);
		return ret;
	}
}
