package org.kissfarmops.agent.action_executor.impl_folder;

import java.io.Serializable;
import java.util.Map;

import org.kissfarmops.shared.api.ActionCommands;

public class ActionInvocationInfo implements Serializable {
	private static final long serialVersionUID = 1040465252595270960L;

	private String name;
	private String executionId;
	private String scriptsFolder;
	private ActionCommands actionCommands;
	private String instanceFolder;
	private Map<String, String> envVars;

	public ActionInvocationInfo() {
	}

	public ActionInvocationInfo(String name, String executionId, String scriptsFolder, ActionCommands actionCommands,
			String instanceFolder, Map<String, String> envVars) {
		this.name = name;
		this.executionId = executionId;
		this.scriptsFolder = scriptsFolder;
		this.actionCommands = actionCommands;
		this.instanceFolder = instanceFolder;
		this.envVars = envVars;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getExecutionId() {
		return executionId;
	}

	public void setExecutionId(String executionId) {
		this.executionId = executionId;
	}

	public String getScriptsFolder() {
		return scriptsFolder;
	}

	public void setScriptsFolder(String scriptsFolder) {
		this.scriptsFolder = scriptsFolder;
	}

	public ActionCommands getActionCommands() {
		return actionCommands;
	}

	public void setActionCommands(ActionCommands actionCommands) {
		this.actionCommands = actionCommands;
	}

	public String getInstanceFolder() {
		return instanceFolder;
	}

	public void setInstanceFolder(String instanceFolder) {
		this.instanceFolder = instanceFolder;
	}

	public Map<String, String> getEnvVars() {
		return envVars;
	}

	public void setEnvVars(Map<String, String> envVars) {
		this.envVars = envVars;
	}
}