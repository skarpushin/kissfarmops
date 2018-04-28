package org.kissfarmops.shared.config.api;

import java.io.Serializable;
import java.util.Map;

import org.kissfarmops.shared.api.EnvVars;

import lombok.Data;

@Data
public class AppDefinitionConfig implements Serializable {
	private static final long serialVersionUID = 6926917970783064166L;

	public static final String ACTION_GET_STATUS = "getStatus";

	private String name;
	private EnvVars env;
	private Map<String, ActionConfig> actions;
	private Map<String, AppPrototypeConfig> prototypes;
}
