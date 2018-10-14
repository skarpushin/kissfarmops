package org.kissfarm.controller.websockets;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.kissfarm.shared.websocket.api.NodeConnectedEvent;
import org.kissfarm.shared.websocket.api.NodeDisconnectedEvent;
import org.springframework.messaging.MessageHeaders;
import org.summerb.approaches.jdbccrud.common.DtoBase;

/**
 * This service processes events from node and propagates them to the UI
 * 
 * @author Sergey Karpushin
 *
 */
public class NodeEventsUiNotifier {
	private Set<Class<?>> allowedClassesForUi = new HashSet<>(
			Arrays.asList(NodeConnectedEvent.class, NodeDisconnectedEvent.class));

	/**
	 * @return message that will be forwarded to UI by Spring integration. See
	 *         integration.xml
	 */
	public DtoBase handleMessage(DtoBase payload, MessageHeaders messageHeaders) {
		if (allowedClassesForUi.contains(payload.getClass())) {
			return payload;
		}
		return null;
	}
}
