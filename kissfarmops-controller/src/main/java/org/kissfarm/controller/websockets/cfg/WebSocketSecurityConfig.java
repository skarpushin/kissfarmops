
package org.kissfarm.controller.websockets.cfg;

import org.kissfarm.controller.security.SecurityConstantsEx;
import org.kissfarmops.shared.websocket.WebSocketCommons;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;

@Configuration
public class WebSocketSecurityConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer {

	// Add security: http://www.baeldung.com/spring-security-websockets

	@Override
	protected boolean sameOriginDisabled() {
		return true; // we don't want a mess with CSRF tokens
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
		// messages.simpDestMatchers("/secured/**").authenticated().anyMessage().authenticated();
		messages.simpDestMatchers(WebSocketCommons.getNodeToServerBasePath() + "/**").hasRole(SecurityConstantsEx.NODE)
				.simpSubscribeDestMatchers(WebSocketCommons.getServerToNodeTopic() + "/**")
				.hasRole(SecurityConstantsEx.NODE).anyMessage().authenticated();

		// TODO: Allow node to subscribe only to it's personal channel, don't allow to
		// listen to other channels

		// TODO: Correctly restrict workflow managers activity - allow to listen
		// certain channels and don';t allow to write to certain channels'
	}

}