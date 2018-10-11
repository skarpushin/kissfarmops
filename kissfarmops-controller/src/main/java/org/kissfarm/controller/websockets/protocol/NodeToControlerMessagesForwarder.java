package org.kissfarm.controller.websockets.protocol;

import org.kissfarm.shared.websocket.WebSocketCommons;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.summerb.approaches.jdbccrud.common.DtoBase;

/**
 * This Controller will get messages from STOMP and forward it to Integration
 * context using {@link StompInboundGateway} (which is automatically impl by
 * Spring-generated proxy)
 * 
 * @author Sergey Karpushin
 *
 */
@Controller
public class NodeToControlerMessagesForwarder {
	private Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	private StompInboundGateway stompInboundGateway;

	@MessageMapping(WebSocketCommons.NTOS_REQUESTS)
	public void onMessageFromNode(DtoBase payload) {
		log.debug("Got message form Node {}", payload);
		stompInboundGateway.propagate(payload);
	}
}
