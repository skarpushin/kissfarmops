package org.kissfarm.controller.websockets;

import org.kissmachine.api.machine.StateMachine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.summerb.approaches.jdbccrud.common.DtoBase;

/**
 * This service will handle requests from node
 * 
 * @author Sergey Karpushin
 *
 */
public class NodeRequestsProcessor {
	private Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	private StateMachine farmConfigStateMachine;

	public void handleMessage(DtoBase payload, MessageHeaders messageHeaders) {
		log.debug("Got message from Node. Message = {}", payload);

		Message<DtoBase> msg = MessageBuilder.withPayload(payload).copyHeaders(messageHeaders).build();

		// TBD: Not all messages from the node will be related to config only. So we
		// need to do some routing here
		farmConfigStateMachine.sendEvent(msg);
	}
}
