package org.kissfarm.controller.websockets;

import org.kissfarm.controller.websockets.api.StompOutboundGateway;
import org.kissfarm.shared.websocket.api.NodeConfigRequest;
import org.kissfarm.shared.websocket.api.NodeConfigResponse;
import org.kissmachine.api.machine.StateMachine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
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
	private StompOutboundGateway stompOutboundGateway;
	@Autowired
	private StateMachine farmConfigStateMachine;

	public void handleMessage(DtoBase payload, MessageHeaders messageHeaders) {
		log.debug("Got message from Node. Message = {}", payload);

		User user = null;
		String nodeId = null;
		try {
			user = securityContextResolver.getUser();
			nodeId = user.getUuid();
		} catch (Throwable t) {
			log.warn("Failed to resolve user", t);
			return;
		}

		Message<DtoBase> msg = MessageBuilder.withPayload(payload).copyHeaders(messageHeaders).build();

		// TODO: Not all messages from the node will be related to config only. So we
		// need to do some routing here
		farmConfigStateMachine.sendEvent(msg);
	}

	private void onNodeNeedsConfig(String nodeId, NodeConfigRequest request) {
		stompOutboundGateway.sendToNode(nodeId, new NodeConfigResponse());
	}
}
