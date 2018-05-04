package org.kissfarm.controller.websockets;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.kissfarm.controller.services.nodes.api.NodeStatus;
import org.kissfarm.controller.services.nodes.api.NodeStatusService;
import org.kissfarmops.shared.websocket.api.NodeConnectedEvent;
import org.kissfarmops.shared.websocket.api.NodeDisconnectedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.summerb.approaches.jdbccrud.common.DtoBase;

import com.google.common.base.Predicate;

/**
 * This service processes events from node and puts it to DB if needed
 * 
 * @author Sergey Karpushin
 *
 */
public class NodeEventsPersist implements InitializingBean {
	private Logger log = LoggerFactory.getLogger(getClass());

	// TODO: Use Guava's EventBus instead of these handlers
	private List<ConditionalConsumer<?>> handlers = new ArrayList<>();

	@Autowired
	private NodeStatusService nodeStatusService;

	@Override
	public void afterPropertiesSet() throws Exception {
		handlers.add(onNodeConnectedEvent);
		handlers.add(onNodeDisconnectedEvent);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void onEvent(@Payload DtoBase payload) {
		for (ConditionalConsumer cc : handlers) {
			if (cc.apply(payload)) {
				cc.accept(payload);
				return;
			}
		}
	}

	private ConditionalConsumer<NodeConnectedEvent> onNodeConnectedEvent = new ConditionalConsumer<NodeConnectedEvent>(
			NodeConnectedEvent.class) {
		@Override
		public void accept(NodeConnectedEvent t) {
			updateNodeStatus(t.getNodeId(), true);
		}
	};

	private ConditionalConsumer<NodeDisconnectedEvent> onNodeDisconnectedEvent = new ConditionalConsumer<NodeDisconnectedEvent>(
			NodeDisconnectedEvent.class) {
		@Override
		public void accept(NodeDisconnectedEvent t) {
			updateNodeStatus(t.getNodeId(), false);
		}
	};

	private void updateNodeStatus(String nodeId, boolean status) {
		try {
			NodeStatus dto = nodeStatusService.findById(nodeId);
			if (dto == null) {
				dto = new NodeStatus();
				dto.setId(nodeId);
				dto.setOnline(status);
				nodeStatusService.create(dto);
			} else {
				dto.setOnline(status);
				nodeStatusService.update(dto);
			}
		} catch (Throwable e) {
			log.warn("Failed to update node " + nodeId + " status to " + status, e);
		}
	}

	public static abstract class ConditionalConsumer<T> implements Predicate<Object>, Consumer<T> {
		private Class<T> clazz;

		public ConditionalConsumer(Class<T> clazz) {
			this.clazz = clazz;
		}

		@Override
		public boolean apply(Object input) {
			return input != null && clazz.isAssignableFrom(input.getClass());
		}
	}
}
