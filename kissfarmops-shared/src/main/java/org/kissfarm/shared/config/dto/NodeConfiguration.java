package org.kissfarm.shared.config.dto;

import java.io.Serializable;
import java.util.Map;

public class NodeConfiguration implements Serializable {
	private static final long serialVersionUID = 1595416671193257945L;

	private String version;
	private Map<String, AppDefConfig> appDefs;

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public Map<String, AppDefConfig> getAppDefs() {
		return appDefs;
	}

	public void setAppDefs(Map<String, AppDefConfig> appDefs) {
		this.appDefs = appDefs;
	}
}
