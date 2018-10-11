package org.kissfarm.shared.websocket.api;

import org.summerb.approaches.jdbccrud.common.DtoBase;

public class NodeDisconnectedEvent implements DtoBase {
	private static final long serialVersionUID = -4906307147224623039L;

	private String nodeId;

	public NodeDisconnectedEvent() {
	}

	public NodeDisconnectedEvent(String nodeId) {
		super();
		this.nodeId = nodeId;
	}

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	@Override
	public String toString() {
		return "NodeDisconnectedEvent [nodeId=" + nodeId + "]";
	}
}
