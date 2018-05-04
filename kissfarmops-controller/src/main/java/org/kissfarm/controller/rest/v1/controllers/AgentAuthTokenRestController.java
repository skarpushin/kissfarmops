package org.kissfarm.controller.rest.v1.controllers;

import org.kissfarm.controller.services.agent_auth_token.api.AgentAuthToken;
import org.kissfarm.controller.services.agent_auth_token.api.AgentAuthTokenService;
import org.kissfarm.controller.services.agent_auth_token.impl.AgentAuthTokenAuthStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.summerb.approaches.jdbccrud.rest.EasyCrudRestControllerBase;
import org.summerb.approaches.jdbccrud.rest.permissions.PermissionsResolverStrategyTableAuthImpl;
import org.summerb.approaches.springmvc.security.SecurityConstants;

@RestController
@Secured(SecurityConstants.ROLE_USER)
@RequestMapping(path = "/rest/api/v1/agent-auth-token")
public class AgentAuthTokenRestController
		extends EasyCrudRestControllerBase<String, AgentAuthToken, AgentAuthTokenService> {
	@Autowired
	private AgentAuthTokenAuthStrategy authStrategy;

	public AgentAuthTokenRestController(AgentAuthTokenService service) {
		super(service);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		permissionsResolverStrategy = new PermissionsResolverStrategyTableAuthImpl<>(authStrategy);
	}
}
