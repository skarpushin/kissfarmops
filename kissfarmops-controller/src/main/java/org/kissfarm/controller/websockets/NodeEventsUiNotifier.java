package org.kissfarm.controller.websockets;

import org.springframework.messaging.MessageHeaders;
import org.summerb.approaches.jdbccrud.common.DtoBase;

/**
 * This service processes events from node and propagates them to the UI
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
		// TBD: Filter. Send only messages useful for UI
		return payload;
	}
}
