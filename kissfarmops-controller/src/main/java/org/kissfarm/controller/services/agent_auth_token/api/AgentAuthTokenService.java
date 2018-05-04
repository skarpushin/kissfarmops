package org.kissfarm.controller.services.agent_auth_token.api;

import org.summerb.approaches.jdbccrud.api.EasyCrudService;

public interface AgentAuthTokenService extends EasyCrudService<String, AgentAuthToken> {
	public static final String TERM = "term.agentAuthToken";
}
