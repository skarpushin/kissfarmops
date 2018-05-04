package org.kissfarm.agent.client.websocket;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

import org.kissfarm.agent.lifecycle.WsConnectLifecycle;
import org.kissfarm.shared.websocket.WebSocketCommons;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.summerb.approaches.jdbccrud.common.DtoBase;

/**
 * 
 * Difference between this and {@link StompSession} is that this class has
 * facilities to notify consumers if session is closed (connection lost)
 * 
 * Counterpart of this logic is located here:
 * {@link WsConnectLifecycle.SessionHandler}
 * 
 * @author Sergey Karpushin
 *
 */
public class StompSessionEvt {
	// private Logger log = LoggerFactory.getLogger(getClass());

	private StompSession stompSession;
	private Collection<Consumer<StompSessionEvt>> connectionLostListeners = new ConcurrentLinkedQueue<>();

	public StompSessionEvt() {
		// no session
	}

	public StompSessionEvt(StompSession stompSession) {
		this.stompSession = stompSession;
	}

	public <T extends DtoBase> void send(String destination, T payload) {
		StompHeaders stompHeaders = new StompHeaders();
		stompHeaders.add(WebSocketCommons.ATTR_PAYLOAD_TYPE, payload.getClass().getName());
		stompHeaders.setDestination(destination);
		getStompSession().send(stompHeaders, payload);
	}

	public boolean isConnected() {
		return stompSession != null && stompSession.isConnected();
	}

	public void addConnectionLostListener(Consumer<StompSessionEvt> runnable) {
		connectionLostListeners.add(runnable);
	}

	public void removeConnectionLostListener(Consumer<StompSessionEvt> runnable) {
		connectionLostListeners.remove(runnable);
	}

	public void fireConnectionLost() {
		List<Consumer<StompSessionEvt>> copy = new ArrayList<>(connectionLostListeners);
		copy.forEach(x -> x.accept(this));
	}

	public StompSession getStompSession() {
		return stompSession;
	}

	public void setStompSession(StompSession newSession) {
		this.stompSession = newSession;
	}
}
