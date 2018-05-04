package org.kissfarm.controller.rest.v1.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.kissfarm.controller.services.nodes.api.Node;
import org.kissfarm.controller.services.nodes.api.NodeAuthStrategy;
import org.kissfarm.controller.services.nodes.api.NodeService;
import org.kissfarm.controller.services.nodes.api.NodeTag;
import org.kissfarm.controller.services.nodes.api.NodeTagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.summerb.approaches.jdbccrud.api.dto.PagerParams;
import org.summerb.approaches.jdbccrud.api.query.Query;
import org.summerb.approaches.jdbccrud.rest.EasyCrudRestControllerBase;
import org.summerb.approaches.jdbccrud.rest.permissions.PermissionsResolverStrategyTableAuthImpl;
import org.summerb.approaches.springmvc.security.SecurityConstants;

@RestController
@Secured(SecurityConstants.ROLE_USER)
@RequestMapping(path = "/rest/api/v1/node")
public class NodesRestController extends EasyCrudRestControllerBase<String, Node, NodeService> {

	@Autowired
	private NodeAuthStrategy authStrategy;

	@Autowired
	private NodeTagService nodeTagService;

	public NodesRestController(NodeService service) {
		super(service);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		permissionsResolverStrategy = new PermissionsResolverStrategyTableAuthImpl<>(authStrategy);
	}

	@PutMapping("{id}/tags")
	public void setTags(@PathVariable("id") String nodeId, @RequestBody List<String> tags) throws Exception {
		nodeTagService.setSubjectTags(nodeId, tags);
	}

	@GetMapping("{id}/tags")
	public List<String> getTags(@PathVariable("id") String nodeId) throws Exception {
		List<NodeTag> tags = nodeTagService.query(PagerParams.ALL, Query.n().eq(NodeTag.FN_SUBJECT_ID, nodeId))
				.getItems();
		return tags.stream().map(x -> x.getTag()).collect(Collectors.toList());
	}

}
