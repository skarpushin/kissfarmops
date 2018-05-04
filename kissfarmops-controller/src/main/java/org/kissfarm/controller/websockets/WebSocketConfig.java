package org.kissfarm.controller.websockets;

import java.util.List;

import org.kissfarm.controller.security.SecurityConstantsEx;
import org.kissfarmops.shared.websocket.WebSocketCommons;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.session.MapSession;
import org.springframework.session.web.socket.config.annotation.AbstractSessionWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.server.HandshakeHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import org.summerb.approaches.security.api.SecurityContextResolver;
import org.summerb.microservices.users.api.dto.User;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig extends AbstractSessionWebSocketMessageBrokerConfigurer<MapSession> {
	private Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	private SecurityContextResolver<User> securityContextResolver;

	@Override
	public void configureMessageBroker(MessageBrokerRegistry config) {
		config.enableSimpleBroker(WebSocketCommons.getServerToNodeTopic(), WebSocketCommons.getServerToUiTopic());
		config.setApplicationDestinationPrefixes(WebSocketCommons.getNodeToServerBasePath());
	}

	@Override
	protected void configureStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint(WebSocketCommons.getEndPointRelativePathForServer()).setHandshakeHandler(handshakeHandler)
				.setAllowedOrigins("*").withSockJS();
	}

	private HandshakeHandler handshakeHandler = new DefaultHandshakeHandler() {
		@Override
		protected boolean isValidOrigin(ServerHttpRequest request) {
			boolean isAllowedToConnect = securityContextResolver.hasAnyRole(SecurityConstantsEx.ROLE_NODE,
					SecurityConstantsEx.ROLE_USER);
			log.debug("Answered {} to user {} who tried to use web sockets", isAllowedToConnect,
					securityContextResolver.resolveSecurityContext().getAuthentication());
			return isAllowedToConnect;
		};
	};

	@Override
	public boolean configureMessageConverters(List<MessageConverter> messageConverters) {
		messageConverters.add(WebSocketCommons.getMessageConverter());
		return false;
	}
}