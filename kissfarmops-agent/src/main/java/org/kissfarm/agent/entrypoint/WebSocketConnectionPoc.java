package org.kissfarm.agent.entrypoint;

import java.util.concurrent.TimeUnit;

import org.kissfarmops.shared.websocket.WebSocketCommons;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

public class WebSocketConnectionPoc {
	private static Logger log = LoggerFactory.getLogger(WebSocketConnectionPoc.class);

	private TaskScheduler taskScheduler = new ConcurrentTaskScheduler();

	public static void main(String[] args) throws Exception {
		WebSocketConnectionPoc f = new WebSocketConnectionPoc();
		StompSession stompSession = f.connectToWebSocket();
		stompSession.send(WebSocketCommons.getNodeToServerBasePath() + "/hello", "WebSocketConnectionPoc first scream");
		stompSession.disconnect();
	}

	private StompSession connectToWebSocket() throws Exception {
		String url = "http://localhost:8080".replaceAll("http", "ws")
				+ WebSocketCommons.getEndPointRelativePathForClient();

		log.info("Connecting to " + url);
		WebSocketClient webSocketClient = new StandardWebSocketClient();
		WebSocketStompClient stompClient = new WebSocketStompClient(webSocketClient);
		stompClient.setMessageConverter(WebSocketCommons.getMessageConverter());
		stompClient.setTaskScheduler(taskScheduler); // for heartbeats

		StompSessionHandler sessionHandler = new StompSessionHandlerAdapter() {
			// do nothing here
		};
		WebSocketHttpHeaders handshakeHeaders = new WebSocketHttpHeaders();

		// TODO: Add session id
		// handshakeHeaders.add("SESSION", api.getCookieStore().getCookies().stream()
		// .filter(x -> x.getName().equals("SESSION")).map(x ->
		// x.getValue()).findFirst().orElse(null));
		handshakeHeaders.add("Cookie", "session=1c785f79-135f-437c-a5a2-35626b058a81");

		StompHeaders stompHeaders = new StompHeaders();
		ListenableFuture<StompSession> sessionFuture = stompClient.connect(url, handshakeHeaders, stompHeaders,
				sessionHandler);

		// wait init
		StompSession session = sessionFuture.get(30, TimeUnit.MINUTES);
		// assertNotNull(session != null && session.isConnected());
		return session;
	}
}
