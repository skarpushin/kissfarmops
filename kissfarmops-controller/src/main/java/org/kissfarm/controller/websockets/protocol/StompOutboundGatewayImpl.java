package org.kissfarm.controller.websockets.protocol;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import org.kissfarm.controller.security.SecurityConstantsEx;
import org.kissfarm.controller.websockets.api.StompOutboundGateway;
import org.kissfarmops.shared.websocket.WebSocketCommons;
import org.kissfarmops.shared.websocket.api.NodeConnectedEvent;
import org.kissfarmops.shared.websocket.api.NodeDisconnectedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.integration.security.channel.SecurityContextPropagationChannelInterceptor;
import org.springframework.jmx.export.annotation.ManagedMetric;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.util.CollectionUtils;
import org.springframework.web.socket.messaging.AbstractSubProtocolEvent;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;
import org.summerb.approaches.jdbccrud.common.DtoBase;
import org.summerb.approaches.springmvc.security.apis.ElevationRunner;
import org.summerb.approaches.springmvc.security.apis.ElevationStrategy;
import org.summerb.approaches.springmvc.security.dto.UserDetailsImpl;
import org.summerb.approaches.springmvc.security.elevation.ElevationRunnerImpl;

import com.google.common.base.Preconditions;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

@ManagedResource
public class StompOutboundGatewayImpl implements StompOutboundGateway, ApplicationListener<AbstractSubProtocolEvent> {
	private Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	private SimpMessagingTemplate messagingTemplate;
	@Autowired
	private StompInboundGateway stompInboundGateway;

	private AtomicInteger connections = new AtomicInteger();

	/**
	 * We track for each subscription what sessions are active. If there are no
	 * sessions actually subscribed for certain topic then we don't want to waste
	 * resources processing messages
	 */
	private Multimap<String, String> subscriptionsToSessions = MultimapBuilder.treeKeys().arrayListValues().build();

	@Override
	public void sendToUi(DtoBase payload) {
		try {
			String destination = WebSocketCommons.getServerToUiTopic();
			boolean hasSubscription;
			synchronized (subscriptionsToSessions) {
				hasSubscription = subscriptionsToSessions.containsKey(destination);
				if (!hasSubscription) {
					return;
				}
			}

			messagingTemplate.convertAndSend(destination, payload,
					Collections.singletonMap(WebSocketCommons.ATTR_PAYLOAD_TYPE, payload.getClass().getName()));
		} catch (Throwable t) {
			log.warn("Failed to send message to UI's WebSocket", t);
		}
	}

	@Override
	public <T extends DtoBase> void sendToNode(String nodeId, T payload) {
		try {
			String destination = WebSocketCommons.getServerToNodeTopic(nodeId);
			boolean hasSubscription;
			synchronized (subscriptionsToSessions) {
				hasSubscription = subscriptionsToSessions.containsKey(destination);
				if (!hasSubscription) {
					return;
				}
			}

			messagingTemplate.convertAndSend(destination, payload,
					Collections.singletonMap(WebSocketCommons.ATTR_PAYLOAD_TYPE, payload.getClass().getName()));
		} catch (Throwable t) {
			log.warn("Failed to send message to Nodes's WebSocket", t);
		}
	}

	@Override
	public void onApplicationEvent(AbstractSubProtocolEvent eventGeneric) {
		if (eventGeneric instanceof SessionSubscribeEvent) {
			onSessionSubscribeEvent((SessionSubscribeEvent) eventGeneric);
		} else if (eventGeneric instanceof SessionUnsubscribeEvent) {
			onSessionUnsubscribeEvent((SessionUnsubscribeEvent) eventGeneric);
		} else if (eventGeneric instanceof SessionDisconnectEvent) {
			onSessionDisconnectEvent((SessionDisconnectEvent) eventGeneric);
		} else if (eventGeneric instanceof SessionConnectedEvent) {
			onSessionConnectedEvent((SessionConnectedEvent) eventGeneric);
		}
	}

	private void onSessionConnectedEvent(SessionConnectedEvent event) {
		connections.incrementAndGet();
		String sessionId = event.getMessage().getHeaders().get("simpSessionId").toString();

		log.debug("WebSocket session {} connected", sessionId);
		UserDetailsImpl user = findUser(event);
		if (isUserRepresentsNode(user)) {
			NodeConnectedEvent derrivedEvent = new NodeConnectedEvent(user.getUser().getUuid());
			sendEventElevated(derrivedEvent, (Authentication) event.getUser());
		}
	}

	private void onSessionDisconnectEvent(SessionDisconnectEvent event) {
		connections.decrementAndGet();

		String sessionId = event.getMessage().getHeaders().get("simpSessionId").toString();
		removeSubscriptionsFroSession(sessionId);
		log.debug("WebSocket session {} disconnected", sessionId);

		UserDetailsImpl user = findUser(event);
		if (isUserRepresentsNode(user)) {
			NodeDisconnectedEvent derrivedEvent = new NodeDisconnectedEvent(user.getUser().getUuid());
			sendEventElevated(derrivedEvent, (Authentication) event.getUser());
		}
	}

	private boolean isUserRepresentsNode(UserDetailsImpl user) {
		if (user == null || CollectionUtils.isEmpty(user.getAuthorities())) {
			return false;
		}

		return user.getAuthorities().stream().anyMatch(x -> SecurityConstantsEx.ROLE_NODE.equals(x.getAuthority()));
	}

	/**
	 * We need to put Authentication into SecurityContext so that
	 * {@link SecurityContextPropagationChannelInterceptor} will pick it up and set
	 * before message will be handled
	 */
	private void sendEventElevated(DtoBase payload, Authentication authentication) {
		ElevationStrategy elevationStrategy = new ElevationStrategyAuthenticationImpl(authentication);
		ElevationRunner elevationRunner = new ElevationRunnerImpl(elevationStrategy);
		elevationRunner.runElevated(() -> stompInboundGateway.propagate(payload));
	}

	private UserDetailsImpl findUser(AbstractSubProtocolEvent event) {
		try {
			if (!(event.getUser() instanceof UsernamePasswordAuthenticationToken)) {
				return null;
			}
			UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) event.getUser();
			Preconditions.checkState(auth != null);
			Preconditions.checkState(auth.getPrincipal() instanceof UserDetailsImpl);
			UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
			Preconditions.checkState(userDetails.getUser() != null);
			return userDetails;
		} catch (Throwable t) {
			log.warn("Failed to extract user from STOMP event " + event, t);
			return null;
		}
	}

	private void onSessionSubscribeEvent(SessionSubscribeEvent event) {
		String destination = (String) event.getMessage().getHeaders().get("simpDestination");
		String sessionId = event.getMessage().getHeaders().get("simpSessionId").toString();

		synchronized (subscriptionsToSessions) {
			subscriptionsToSessions.put(destination, sessionId);
		}

		log.debug("WebSocket session {} subscribed to {}", sessionId, destination);
	}

	private void onSessionUnsubscribeEvent(SessionUnsubscribeEvent event) {
		String sessionId = event.getMessage().getHeaders().get("simpSessionId").toString();

		// TODO: Clarify what channel was unsubscribed.
		removeSubscriptionsFroSession(sessionId);

		log.debug("WebSocket session {} unsubscribed", sessionId);
	}

	private void removeSubscriptionsFroSession(String sessionId) {
		synchronized (subscriptionsToSessions) {
			if (!subscriptionsToSessions.containsValue(sessionId)) {
				return;
			}

			for (Iterator<Entry<String, String>> iter = subscriptionsToSessions.entries().iterator(); iter.hasNext();) {
				Entry<String, String> entry = iter.next();
				if (sessionId.equals(entry.getValue())) {
					iter.remove();
				}
			}
		}
	}

	@ManagedMetric
	public int getConnectionsCount() {
		return connections.get();
	}
}
