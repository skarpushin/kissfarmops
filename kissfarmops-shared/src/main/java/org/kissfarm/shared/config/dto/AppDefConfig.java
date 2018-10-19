package org.kissfarm.shared.config.dto;

import java.io.Serializable;
import java.util.Map;

/**
 * Application Definition Config
 * 
 * @author Sergey Karpushin
 *
 */
public class AppDefConfig implements Serializable {
	private static final long serialVersionUID = 6926917970783064166L;

	public static final String ACTION_GET_STATUS = "getStatus";

	private String name;
	private String displayName;
	private EnvVars env;
	private Map<String, ActionConfig> actions;
	private Map<String, AppProtoConfig> prototypes;
	private StatusSchema statusSchema;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public EnvVars getEnv() {
		return env;
	}

	public void setEnv(EnvVars env) {
		this.env = env;
	}

	public Map<String, ActionConfig> getActions() {
		return actions;
	}

	public void setActions(Map<String, ActionConfig> actions) {
		this.actions = actions;
	}

	public Map<String, AppProtoConfig> getPrototypes() {
		return prototypes;
	}

	public void setPrototypes(Map<String, AppProtoConfig> prototypes) {
		this.prototypes = prototypes;
	}

	public StatusSchema getStatusSchema() {
		return statusSchema;
	}

	public void setStatusSchema(StatusSchema statusSchema) {
		this.statusSchema = statusSchema;
	}
}
