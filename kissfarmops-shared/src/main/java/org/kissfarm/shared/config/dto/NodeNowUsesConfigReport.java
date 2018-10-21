package org.kissfarm.shared.config.dto;

import org.summerb.approaches.jdbccrud.common.DtoBase;

/**
 * Node (Agent) will report this when new configuration will be in effect
 * 
 * @author Sergey Karpushin
 *
 */
public class NodeNowUsesConfigReport implements DtoBase {
	private static final long serialVersionUID = 4426456425226234004L;

	private String nodeId;
	private String version;

	public NodeNowUsesConfigReport() {
	}

	public NodeNowUsesConfigReport(String nodeId, String version) {
		super();
		this.nodeId = nodeId;
		this.version = version;
	}

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	@Override
	public String toString() {
		return "NodeNowUsesConfigReport [nodeId=" + nodeId + ", version=" + version + "]";
	}
}
