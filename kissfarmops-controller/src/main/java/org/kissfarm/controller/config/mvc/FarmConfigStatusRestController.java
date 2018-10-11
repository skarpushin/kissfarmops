package org.kissfarm.controller.config.mvc;

import java.util.HashMap;
import java.util.Map;

import org.kissfarm.controller.config.smachine.dtos.FarmConfigMachineVariables;
import org.kissmachine.api.dto.SmStateData;
import org.kissmachine.api.easycrud.SmStateDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.summerb.approaches.jdbccrud.api.query.Query;
import org.summerb.approaches.jdbccrud.rest.EasyCrudRestControllerBase;
import org.summerb.approaches.jdbccrud.rest.commonpathvars.PathVariablesMap;
import org.summerb.approaches.jdbccrud.rest.dto.SingleItemResult;
import org.summerb.approaches.jdbccrud.rest.permissions.PermissionsResolverPerTable;
import org.summerb.approaches.jdbccrud.rest.permissions.PermissionsResolverStrategyTableAuthImpl;
import org.summerb.approaches.jdbccrud.rest.querynarrower.QueryNarrowerStrategy;
import org.summerb.approaches.security.api.SecurityContextResolver;
import org.summerb.approaches.springmvc.security.SecurityConstants;
import org.summerb.microservices.users.api.dto.User;

/**
 * This is just a read-only rest controller which intent is to provide easy
 * access to the Farm Config state machine state changes
 * 
 * @author sergeyk
 *
 */
@RestController
@Secured(SecurityConstants.ROLE_USER)
@RequestMapping(path = "/rest/api/v1/farm-config/state")
public class FarmConfigStatusRestController
		extends EasyCrudRestControllerBase<String, SmStateData, SmStateDataService> {

	@Autowired
	protected SecurityContextResolver<User> securityContextResolver;

	public FarmConfigStatusRestController(SmStateDataService service) {
		super(service);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		permissionsResolverStrategy = new PermissionsResolverStrategyTableAuthImpl<>(authStrategy);
		queryNarrowerStrategy = buildNarrowerStrategy();
	}

	private PermissionsResolverPerTable authStrategy = new PermissionsResolverPerTable() {
		@Override
		public Map<String, Boolean> resolvePermissions() {
			Map<String, Boolean> ret = new HashMap<>();
			ret.put("create", false);
			ret.put("read", securityContextResolver.hasRole(SecurityConstants.ROLE_USER));
			ret.put("update", false);
			ret.put("delete", false);
			return ret;
		}
	};

	private QueryNarrowerStrategy buildNarrowerStrategy() {
		return new QueryNarrowerStrategy() {
			@Override
			public Query narrow(Query optionalQuery, PathVariablesMap pathVariables) {
				Query ret = optionalQuery != null ? optionalQuery : Query.n();
				ret = ret.eq(SmStateData.FN_MACHINE_TYPE, FarmConfigMachineVariables.MACHINE_TYPE);
				return ret;
			}
		};
	}

	@Override
	public SingleItemResult<String, SmStateData> createNewItem(SmStateData dto, boolean needPerms) throws Exception {
		throw new IllegalStateException("Wrong opeartion exception");
	}

	@Override
	public SingleItemResult<String, SmStateData> updateItem(String id, SmStateData rowToUpdate, boolean needPerms)
			throws Exception {
		throw new IllegalStateException("Wrong opeartion exception");
	}

	@Override
	public void deleteItem(String id) throws Exception {
		throw new IllegalStateException("Wrong opeartion exception");
	}
}
