package org.kissfarm.controller.security.impl;

import org.summerb.approaches.jdbccrud.api.EasyCrudTableAuthStrategy;
import org.summerb.approaches.jdbccrud.rest.permissions.PermissionsResolverPerTable;

public interface AuthenticatedUsersTableAuthStrategy extends EasyCrudTableAuthStrategy, PermissionsResolverPerTable {

}
