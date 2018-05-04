package org.kissfarm.agent.lifecycle;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.kissfarm.agent.client.websocket.StompFrameHandlerPayloadTypeAware;
import org.kissfarm.agent.client.websocket.StompSessionEvt;
import org.kissfarm.agent.client.websocket.StompSessionHolder;
import org.kissfarm.agent.node_identity.api.NodeIdentityHolder;
import org.kissfarm.shared.websocket.WebSocketCommons;
import org.kissfarm.shared.websocket.api.NodeConfigRequest;
import org.kissfarm.shared.websocket.api.NodeConfigResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompSession.Subscription;

import com.google.common.eventbus.Subscribe;

public class InitialConfigLifecycle implements Lifecycle {
	private Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	private NormalLifecycle normalLifecycle;
	@Autowired
	private RegistrationLifecycle registrationLifecycle;
	@Autowired
	private StompSessionHolder stompSessionHolder;
	@Autowired
	private NodeIdentityHolder nodeIdentityHolder;

	private StompSessionEvt session;
	private CompletableFuture<Lifecycle> nextLifeCycle = null;

	private Subscription subscription;

	@Override
	public Lifecycle call() throws Exception {
		subscription = null;

		while (true) {
			session = stompSessionHolder.getSession();
			if (!session.isConnected()) {
				return registrationLifecycle;
			}

			nextLifeCycle = new CompletableFuture<>();
			session.addConnectionLostListener(onConnectionLost);

			// TODO: Try read saved config, we might have one already downloaded

			// Let Controller know we need config
			subscription = session.getStompSession().subscribe(
					WebSocketCommons.getServerToNodeTopic(nodeIdentityHolder.getNodeIdentity().getId()), onMessage);
			session.send(WebSocketCommons.getNodeToServerDestination(), new NodeConfigRequest());

			// TODO: Perform config download
			// TODO: Resume download if applicable

			// TODO: Do we need to monitor connection stability if we switched to next
			// lifecycle??..

			try {
				log.debug("Block and wait for next lifecycle step");
				return nextLifeCycle.get();
				// TODO: Handle exception gracefully and retry....?
			} catch (InterruptedException ie) {
				log.debug("had InterruptedException, going to close agent", ie);
				return null;
			} finally {
				safeUnsubscribe();
			}

			// TODO: Handle JVM normal shutdown and gracefully stop agent
		}
	}

	private void safeUnsubscribe() {
		try {
			if (subscription != null) {
				subscription.unsubscribe();
				subscription = null;
			}
		} catch (Throwable t) {
			log.debug("Failed to unsubscribe", t);
		}
	}

	private StompFrameHandler onMessage = new StompFrameHandlerPayloadTypeAware() {
		@Subscribe
		public void onNodeConfigResponse(NodeConfigResponse dto) {
			log.debug("We got our NodeConfigResponse !!! " + dto);
			nextLifeCycle.complete(null); // TODO: Replace with transition forward
		}
	};

	private Consumer<StompSessionEvt> onConnectionLost = new Consumer<StompSessionEvt>() {
		@Override
		public void accept(StompSessionEvt t) {
			log.debug("Received notification about closed connection");
			t.removeConnectionLostListener(onConnectionLost);
			session = null;
			nextLifeCycle.complete(registrationLifecycle);
		}
	};
}
