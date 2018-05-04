package org.kissfarm.controller.websockets;

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
	/**
	 * @return message that will be forwarded to UI by Spring integration. See
	 *         integration.xml
	 */
	public DtoBase handleMessage(DtoBase payload, MessageHeaders messageHeaders) {
		// TODO: Filter. Send only messages useful for UI
		return payload;
	}
}
