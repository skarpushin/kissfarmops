package org.kissfarm.controller.websockets;

import org.kissfarmops.shared.websocket.WebSocketCommons;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.summerb.approaches.jdbccrud.common.DtoBase;

@Controller
public class NodeEventsReceivingGateway {
	private Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	private EventFromNodePropagator eventFromNodePropagator;

	@MessageMapping(WebSocketCommons.NTOS_REQUESTS)
	public void onMessageFromNode(DtoBase payload) {
		log.debug("Got message {}", payload);
		eventFromNodePropagator.propagate(payload);
	}
}
