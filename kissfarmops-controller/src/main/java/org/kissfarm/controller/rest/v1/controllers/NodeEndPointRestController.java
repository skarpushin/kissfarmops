package org.kissfarm.controller.rest.v1.controllers;

import java.util.List;
import java.util.UUID;

import org.kissfarm.controller.services.nodes.api.Node;
import org.kissfarm.controller.services.nodes.api.NodeService;
import org.kissfarm.controller.services.nodes.api.NodeTagService;
import org.kissfarm.controller.services.nodes.api.TagParser;
import org.kissfarm.shared.api.NodeIdentity;
import org.kissfarm.shared.api.NodeTagsRequiredValidationError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.summerb.approaches.security.api.SecurityContextResolver;
import org.summerb.approaches.springmvc.security.apis.ElevationRunner;
import org.summerb.approaches.springmvc.security.dto.LoginParams;
import org.summerb.approaches.validation.FieldValidationException;
import org.summerb.microservices.users.api.UserService;
import org.summerb.microservices.users.api.dto.User;

import com.google.common.base.Preconditions;

/**
 * This controller is responsible for answering Node's HTTP requests
 * 
 * @author Sergey Karpushin
 *
 */
@RestController
@RequestMapping(path = "/rest/api/v1/node-endpoint")
public class NodeEndPointRestController {
	@Autowired
	private NodeService nodeService;
	@Autowired
	private UserService userService;
	@Autowired
	private NodeTagService nodeTagService;
	@Autowired
	private TagParser tagParser;
	@Autowired
	private SecurityContextResolver<User> securityContextResolver;
	@Autowired
	private ElevationRunner backgroundProcessRightsElevation;

	// TODO: Add method to download configuration. Support resume.

	@PostMapping("register")
	@Transactional // CONSIDER: maybe move to service layer?
	public @ResponseBody LoginParams registerNode(@RequestBody NodeIdentity nodeIdentity,
			@RequestParam("authToken") String authToken) throws Exception {

		Preconditions.checkState(CollectionUtils.isEmpty(securityContextResolver.getCurrentUserGlobalPermissions()),
				"Only anonymous users expected to trigger this method");
		List<String> tags = tagParser.parseTags(nodeIdentity.getTags());
		if (CollectionUtils.isEmpty(tags)) {
			throw new FieldValidationException(new NodeTagsRequiredValidationError());
		}

		Node node = buildNode(nodeIdentity, authToken);

		backgroundProcessRightsElevation.callElevated(() -> {
			// NOTE: Authtoken will be verified by NodeValidationStrategyImpl and if it's
			// valid operation will be authorized
			nodeService.create(node);
			nodeTagService.setSubjectTags(node.getId(), tags);
			return null;
		});

		String nodeAssociatedEmail = userService.getUserByUuid(node.getId()).getEmail();
		return new LoginParams(nodeAssociatedEmail, node.getPassword());
	}

	private Node buildNode(NodeIdentity nodeIdentity, String authToken) {
		Node node = new Node();
		node.setAgentAuthToken(authToken);
		node.setBlocked(false);
		node.setHostName(nodeIdentity.getHostName());
		node.setId(nodeIdentity.getId());
		node.setPassword(UUID.randomUUID().toString());
		node.setPublicIp(nodeIdentity.getPublicIp());
		return node;
	}
}
