package org.kissfarm.controller.config.smachine.states;

import java.io.Serializable;

import org.kissfarm.controller.services.nodes.api.NodeStatus;
import org.kissfarm.controller.services.nodes.api.NodeStatusService;
import org.kissfarm.shared.websocket.api.NodeConfigRequest;
import org.kissfarm.shared.websocket.api.NodeConnectedEvent;
import org.kissfarm.shared.websocket.api.NodeDisconnectedEvent;
import org.kissmachine.api.machine.SmTransitionToState;
import org.kissmachine.impl.state.SmStateAbstract;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.util.ObjectUtils;

import com.google.common.base.Preconditions;

/**
 * Base class for Farm Config states. Some common methods and handlers which
 * should present in all states of this state machine
 * 
 * @author sergeyk
 */
public abstract class FarmConfigStateAbstract<P extends Serializable, S extends Serializable, R extends Serializable, M extends Serializable>
		extends SmStateAbstract<P, S, R, M> {

	@Autowired
	private NodeStatusService nodeStatusService;

	public FarmConfigStateAbstract() {
		super();

		// NOTE: Honestly that's is a BIG stretch that this logic belongs here. It feels
		// like it should be in a little bit other place, but there is a intersection on
		// NodeStatus dto when handling these 2 events and NodeNowUsesConfigReport, so
		// it's nice to have single-threaded
		handleByPayload(NodeConnectedEvent.class, msg -> onNodeConnectedEvent(msg));
		handleByPayload(NodeDisconnectedEvent.class, msg -> onNodeDisconnectedEvent(msg));
		handleByPayload(NodeConfigRequest.class, msg -> onNodeConfigRequest(msg));
	}

	/**
	 * if we got NodeConfigRequest in general it means it might not have any config.
	 * or it is just checking for updates. In either case we should update DB
	 */
	protected SmTransitionToState onNodeConfigRequest(Message<NodeConfigRequest> msg) {
		try {
			NodeConfigRequest payload = msg.getPayload();

			NodeStatus dto = nodeStatusService.findById(payload.getNodeId());
			Preconditions.checkArgument(dto != null,
					"How is it possible. Node is sending us NodeConfigRequest, but it doesn't have a record in DB. Node %s",
					payload.getNodeId());

			if (ObjectUtils.nullSafeEquals(dto.getVersion(), payload.getCurrentVersion())) {
				return null; // no change
			}

			log.debug("Updating node {}, current version: {}, modifiedAt: {}", dto.getId(), payload.getCurrentVersion(),
					dto.getModifiedAt());
			dto.setVersion(payload.getCurrentVersion());
			dto = nodeStatusService.update(dto);
			log.debug("Updated node {}, current version: {}, new modifiedAt: {}", dto.getId(),
					payload.getCurrentVersion(), dto.getModifiedAt());
		} catch (Throwable t) {
			log.warn("Failed to process NodeConfigRequest", t);
		}
		return null;
	}

	protected SmTransitionToState onNodeDisconnectedEvent(Message<NodeDisconnectedEvent> msg) {
		updateNodeStatus(msg.getPayload().getNodeId(), false);
		return null;
	}

	protected SmTransitionToState onNodeConnectedEvent(Message<NodeConnectedEvent> msg) {
		updateNodeStatus(msg.getPayload().getNodeId(), true);
		return null;
	}

	protected void updateNodeStatus(String nodeId, boolean online) {
		try {
			NodeStatus dto = nodeStatusService.findById(nodeId);
			if (dto == null) {
				dto = new NodeStatus();
				dto.setId(nodeId);
				dto.setOnline(online);
				nodeStatusService.create(dto);
			} else {
				log.debug("Updating node {}, online status is: {}, modifiedAt: {}", nodeId, online,
						dto.getModifiedAt());
				dto.setOnline(online);
				dto = nodeStatusService.update(dto);
				log.debug("Updated node {}, online status to {}, new modifiedAt: {}", nodeId, online,
						dto.getModifiedAt());
			}
		} catch (Throwable e) {
			log.warn("Failed to update node " + nodeId + " status to " + online, e);
		}
	}
}
