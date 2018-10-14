package org.kissfarm.agent.config;

import java.util.Map;

import org.kissfarm.shared.config.dto.AppDefConfig;
import org.summerb.approaches.jdbccrud.common.DtoBase;

public class NodeConfig implements DtoBase {
	private static final long serialVersionUID = -5234483858323926587L;

	private String version;
	private Map<String, AppDefConfig> appDefs;
	private String configBasePath;

	public Map<String, AppDefConfig> getAppDefs() {
		return appDefs;
	}

	public void setAppDefs(Map<String, AppDefConfig> appDefs) {
		this.appDefs = appDefs;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getConfigBasePath() {
		return configBasePath;
	}

	public void setConfigBasePath(String configBasePath) {
		this.configBasePath = configBasePath;
	}
}
