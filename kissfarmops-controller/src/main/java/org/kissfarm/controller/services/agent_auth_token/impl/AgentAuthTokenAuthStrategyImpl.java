package org.kissfarm.controller.services.agent_auth_token.impl;

import java.util.HashMap;
import java.util.Map;

import org.kissfarm.controller.security.impl.EasyCrudTableAuthStrategyAbstract;
import org.summerb.approaches.security.api.exceptions.NotAuthorizedException;
import org.summerb.approaches.springmvc.security.SecurityConstants;

public class AgentAuthTokenAuthStrategyImpl extends EasyCrudTableAuthStrategyAbstract
		implements AgentAuthTokenAuthStrategy {

	@Override
	public void assertAuthorizedToRead() throws NotAuthorizedException {
		assertUserRole("agentAuthToken.read", SecurityConstants.ROLE_USER, SecurityConstants.ROLE_BACKGROUND_PROCESS);
	}

	@Override
	protected void assertAuthorizedToModify() throws NotAuthorizedException {
		assertUserRole("agentAuthToken.modify", SecurityConstants.ROLE_USER);
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
