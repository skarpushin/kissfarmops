package org.kissfarm.shared.websocket.api;

import org.summerb.approaches.jdbccrud.common.DtoBase;

public class NodeConfigRequest implements DtoBase {
	private static final long serialVersionUID = 2796252697845890173L;

	private String nodeId;

	/**
	 * Node might be asking for updates -OR- getting it's first version. If this is
	 * just a update check this field will contain current version
	 */
	private String currentVersion;

	public NodeConfigRequest() {
	}

	public NodeConfigRequest(String nodeId, String currentVersion) {
		super();
		this.nodeId = nodeId;
		this.currentVersion = currentVersion;
	}

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	public String getCurrentVersion() {
		return currentVersion;
	}

	public void setCurrentVersion(String currentVersion) {
		this.currentVersion = currentVersion;
	}
}
