package org.kissfarm.agent.lifecycle;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.kissfarm.agent.client.api.ControllerConnection;
import org.kissfarm.agent.client.api.ControllerConnectionInfo;
import org.kissfarm.agent.client.api.ControllerConnectionInfoHolder;
import org.kissfarm.agent.client.websocket.StompSessionEvt;
import org.kissfarm.agent.client.websocket.StompSessionHolder;
import org.kissfarm.shared.websocket.WebSocketCommons;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.stomp.ConnectionLostException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import com.google.common.base.Preconditions;

public class WsConnectLifecycle extends LifecycleSyncBase implements StompSessionHolder {
	@Autowired
	private ControllerConnectionInfoHolder controllerConnectionInfoHolder;
	@Autowired
	private ControllerConnection controllerConnection;

	@Autowired
	private RegistrationLifecycle registrationLifecycle;
	@Autowired
	private InitConfigLifecycle initConfigLifecycle;

	private TaskScheduler taskScheduler = new ConcurrentTaskScheduler();
	private WebSocketStompClient stompClient;

	volatile private StompSessionEvt session = new StompSessionEvt();

	@Override
	protected Lifecycle doStep() throws Exception {
		session = new StompSessionEvt();

		ControllerConnectionInfo info = controllerConnectionInfoHolder.getControllerConnectionInfo();
		if (info == null || info.getLoginParams() == null) {
			return registrationLifecycle;
		}

		String url = info.getBaseUrl().replaceAll("http(s)?", "ws")
				+ WebSocketCommons.getEndPointRelativePathForClient();
		log.info("Connecting to web socket at: " + url);

		WebSocketHttpHeaders handshakeHeaders = new WebSocketHttpHeaders();
		handshakeHeaders.add("Cookie", "session=" + controllerConnection.findSessionId());

		StompHeaders stompHeaders = new StompHeaders();
		ListenableFuture<StompSession> sessionFuture = getStompClient().connect(url, handshakeHeaders, stompHeaders,
				new SessionHandler(session));

		try {
			StompSession newSession = sessionFuture.get(20, TimeUnit.SECONDS);
			Preconditions.checkState(newSession.isConnected(),
					"Got StompSession from stompClient but it's not connected");
			session.setStompSession(newSession);
			return initConfigLifecycle;
		} catch (InterruptedException ie) {
			log.debug("had InterruptedException, going to exit agent", ie);
			return null;
		}
	}

	private WebSocketStompClient getStompClient() {
		if (stompClient == null) {
			WebSocketClient webSocketClient = new StandardWebSocketClient();
			stompClient = new WebSocketStompClient(webSocketClient);
			stompClient.setMessageConverter(WebSocketCommons.getMessageConverter());
			stompClient.setTaskScheduler(taskScheduler); // for heartbeats
		}
		return stompClient;
	}

	public static class SessionHandler extends StompSessionHandlerAdapter {
		private static int idxCounter = 0;
		private String idx = "TBD" + (idxCounter++);

		private Logger log = LoggerFactory.getLogger(getClass());
		private StompSessionEvt sessionEx;

		public SessionHandler(StompSessionEvt session) {
			this.sessionEx = session;
		}

		@Override
		public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
			idx = session.getSessionId();
			log.info("STOMP session {} established", idx);
			super.afterConnected(session, connectedHeaders);
		}

		@Override
		public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload,
				Throwable exception) {
			log.warn("STOMP " + idx + " frame processing exception: " + Arrays.toString(payload), exception);
			super.handleException(session, command, headers, payload, exception);
		}

		@Override
		public void handleTransportError(StompSession session, Throwable exception) {
			if (sessionEx != null && exception instanceof ConnectionLostException) {
				sessionEx.fireConnectionLost();
				log.info("STOMP {} connection lost", idx);
				return;
			}

			log.warn("STOMP " + idx + " Transport error", exception);
			super.handleTransportError(session, exception);
		}
	}

	@Override
	public StompSessionEvt getSession() {
		return session;
	}
}
