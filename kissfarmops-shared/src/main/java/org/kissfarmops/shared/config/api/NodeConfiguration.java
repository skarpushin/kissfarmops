package org.kissfarmops.shared.config.api;

import java.io.Serializable;
import java.util.Map;

import lombok.Data;

@Data
public class NodeConfiguration implements Serializable {
	private static final long serialVersionUID = 1595416671193257945L;

	private String version;
	private Map<String, AppDefinitionConfig> appDefinitions;
}
