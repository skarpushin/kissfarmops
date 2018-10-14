package org.kissfarm.shared.websocket.api;

import org.summerb.approaches.jdbccrud.common.DtoBase;

public class NodeConfigResponse implements DtoBase {
	private static final long serialVersionUID = -2985081488704932252L;

	private String relativeUrlPath;
	private String version;

	public String getRelativeUrlPath() {
		return relativeUrlPath;
	}

	public void setRelativeUrlPath(String url) {
		this.relativeUrlPath = url;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
}
