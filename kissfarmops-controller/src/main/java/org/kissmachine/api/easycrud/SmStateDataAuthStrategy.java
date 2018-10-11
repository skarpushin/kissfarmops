package org.kissmachine.api.easycrud;

import org.summerb.approaches.jdbccrud.api.EasyCrudTableAuthStrategy;
import org.summerb.approaches.jdbccrud.rest.permissions.PermissionsResolverPerTable;

public interface SmStateDataAuthStrategy extends EasyCrudTableAuthStrategy, PermissionsResolverPerTable {

}
