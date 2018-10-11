package org.kissfarm.shared.websocket.api;

import org.summerb.approaches.jdbccrud.common.DtoBase;

public class NodeConnectedEvent implements DtoBase {
	private static final long serialVersionUID = -3852150126673879841L;

	private String nodeId;

	public NodeConnectedEvent() {
	}

	public NodeConnectedEvent(String nodeId) {
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
		return "NodeConnectedEvent [nodeId=" + nodeId + "]";
	}
}
