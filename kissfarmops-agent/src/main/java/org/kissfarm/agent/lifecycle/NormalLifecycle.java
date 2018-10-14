package org.kissfarm.agent.lifecycle;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.kissfarm.agent.client.api.ControllerConnection;
import org.kissfarm.agent.client.websocket.StompFrameHandlerPayloadTypeAware;
import org.kissfarm.agent.client.websocket.StompSessionEvt;
import org.kissfarm.agent.client.websocket.StompSessionHolder;
import org.kissfarm.agent.config.NodeConfigHolder;
import org.kissfarm.agent.node_identity.api.NodeIdentityHolder;
import org.kissfarm.shared.config.dto.NodeNowUsesConfigReport;
import org.kissfarm.shared.websocket.WebSocketCommons;
import org.kissfarm.shared.websocket.api.NodeConfigResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompSession.Subscription;

import com.google.common.eventbus.Subscribe;

public class NormalLifecycle extends LifecycleSyncBase {
	private Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	private RegistrationLifecycle registrationLifecycle;
	@Autowired
	private StompSessionHolder stompSessionHolder;
	@Autowired
	private NodeIdentityHolder nodeIdentityHolder;
	@Autowired
	private NodeConfigHolder nodeConfigHolder;
	@Autowired
	private ControllerConnection controllerConnection;

	private StompSessionEvt session;
	private Subscription subscription;
	private CompletableFuture<Lifecycle> nextLifeCycle = null;

	@Override
	protected Lifecycle doStep() throws Exception {
		session = stompSessionHolder.getSession();
		if (!session.isConnected()) {
			return registrationLifecycle;
		}
		session.addConnectionLostListener(onConnectionLost);

		nextLifeCycle = new CompletableFuture<>();
		String nodeId = nodeIdentityHolder.getNodeIdentity().getId();
		subscription = session.getStompSession().subscribe(WebSocketCommons.getServerToNodeTopic(nodeId), onMessage);

		// TBD: Init App SPIs

		// Notify Controller about our current version
		session.send(WebSocketCommons.getNodeToServerDestination(), new NodeNowUsesConfigReport(
				nodeIdentityHolder.getNodeIdentity().getId(), nodeConfigHolder.getNodeConfig().getVersion()));

		return nextLifeCycle.get();

		// TBD: Handle JVM normal shutdown and gracefully stop agent
	}

	private Consumer<StompSessionEvt> onConnectionLost = new Consumer<StompSessionEvt>() {
		@Override
		public void accept(StompSessionEvt t) {
			log.debug("Received notification about closed connection");
			safeUnsubscribe();
			nextLifeCycle.complete(registrationLifecycle);
		}
	};

	private StompFrameHandler onMessage = new StompFrameHandlerPayloadTypeAware() {
		@Subscribe
		public void onNodeConfigResponse(NodeConfigResponse dto) {
			// TBD: TBD: Handle messages from server
		}
	};

	private void safeUnsubscribe() {
		try {
			if (subscription != null) {
				subscription.unsubscribe();
				subscription = null;
			}
			session.removeConnectionLostListener(onConnectionLost);
			session = null;
		} catch (Throwable t) {
			log.debug("Failed to unsubscribe", t);
		}
	}

}
