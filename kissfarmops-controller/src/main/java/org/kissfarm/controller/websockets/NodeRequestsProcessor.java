package org.kissfarm.controller.websockets;

import org.kissfarmops.shared.api.NodeConfigResult;
import org.kissfarmops.shared.api.NodeNeedsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageHeaders;
import org.summerb.approaches.jdbccrud.common.DtoBase;
import org.summerb.approaches.security.api.SecurityContextResolver;
import org.summerb.microservices.users.api.dto.User;

/**
 * This service will handle requests from node
 * 
 * @author Sergey Karpushin
 *
 */
public class NodeRequestsProcessor {
	private Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	private SecurityContextResolver<User> securityContextResolver;
	@Autowired
	private StompGate stompGate;

	public void handleMessage(DtoBase payload, MessageHeaders messageHeaders) {
		log.debug("on message = {}", payload);

		User user = null;
		try {
			user = securityContextResolver.getUser();
		} catch (Throwable t) {
			log.warn("Failed to resolve user", t);
		}

		if (payload instanceof NodeNeedsConfig) {
			NodeNeedsConfig request = (NodeNeedsConfig) payload;
			onNodeNeedsConfig(user.getUuid(), request);
		}
	}

	private void onNodeNeedsConfig(String nodeId, NodeNeedsConfig request) {
		stompGate.sendToNode(nodeId, new NodeConfigResult());
	}
}
