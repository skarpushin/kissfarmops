package org.kissfarm.controller.services.nodes.api;

import org.summerb.approaches.jdbccrud.api.EasyCrudPerRowAuthStrategy;
import org.summerb.approaches.jdbccrud.rest.permissions.PermissionsResolverPerTable;

public interface NodeAuthStrategy extends EasyCrudPerRowAuthStrategy<Node>, PermissionsResolverPerTable {
}
