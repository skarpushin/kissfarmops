package org.kissfarm.controller.services.agent_auth_token.impl;

import org.summerb.approaches.jdbccrud.api.EasyCrudTableAuthStrategy;
import org.summerb.approaches.jdbccrud.rest.permissions.PermissionsResolverPerTable;

public interface AgentAuthTokenAuthStrategy extends EasyCrudTableAuthStrategy, PermissionsResolverPerTable {

}
