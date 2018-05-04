package org.kissfarm.controller.websockets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.MessageHeaders;
import org.summerb.approaches.jdbccrud.common.DtoBase;

/**
 * This service processes events from node and propagates them to the UI
 * 
 * TODO: Consider using integration patters for transformation and routing -
 * maybe we don't need to create our own class
 * 
 * @author Sergey Karpushin
 *
 */
public class NodeEventsUiNotifier {
	private Logger log = LoggerFactory.getLogger(getClass());

	public DtoBase handleMessage(DtoBase payload, MessageHeaders messageHeaders) {
		// TODO: Filter. Send only messages useful for UI
		return payload;
	}
}
