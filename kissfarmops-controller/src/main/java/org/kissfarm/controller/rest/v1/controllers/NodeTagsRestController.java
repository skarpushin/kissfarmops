package org.kissfarm.controller.rest.v1.controllers;

import org.kissfarm.controller.services.nodes.api.NodeTag;
import org.kissfarm.controller.services.nodes.api.NodeTagAuthStrategy;
import org.kissfarm.controller.services.nodes.api.NodeTagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.summerb.approaches.jdbccrud.rest.EasyCrudRestControllerBase;
import org.summerb.approaches.jdbccrud.rest.permissions.PermissionsResolverStrategyTableAuthImpl;
import org.summerb.approaches.springmvc.security.SecurityConstants;

@RestController
@Secured(SecurityConstants.ROLE_USER)
@RequestMapping(path = "/rest/api/v1/node-tag")
public class NodeTagsRestController extends EasyCrudRestControllerBase<Long, NodeTag, NodeTagService> {

	@Autowired
	private NodeTagAuthStrategy authStrategy;

	public NodeTagsRestController(NodeTagService service) {
		super(service);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		permissionsResolverStrategy = new PermissionsResolverStrategyTableAuthImpl<>(authStrategy);
	}

}
