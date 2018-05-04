package org.kissfarm.controller.websockets;

import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.session.web.socket.events.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.summerb.approaches.jdbccrud.common.DtoBase;

/**
 * When node-related event was discovered programmatically (i.e. see
 * {@link StompGateImpl}, particularly {@link SessionConnectEvent} or
 * {@link SessionDisconnectEvent}) this method can be used to post message to
 * common message flow regarding node
 * 
 * @author Sergey Karpushin
 *
 */
public interface EventFromNodePropagator {
	<T extends DtoBase> void propagate(@Payload T payload);
}
