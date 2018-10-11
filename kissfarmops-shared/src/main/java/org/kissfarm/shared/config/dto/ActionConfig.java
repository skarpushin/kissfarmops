package org.kissfarm.shared.config.dto;

import java.io.Serializable;
import java.util.List;

public class ActionConfig implements Serializable {
	private static final long serialVersionUID = 523263258527605358L;

	private String name;
	private List<String> parameterNames;
	private ActionCommands commands;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getParameterNames() {
		return parameterNames;
	}

	public void setParameterNames(List<String> parameterNames) {
		this.parameterNames = parameterNames;
	}

	public ActionCommands getCommands() {
		return commands;
	}

	public void setCommands(ActionCommands commands) {
		this.commands = commands;
	}
}
