package org.kissfarmops.shared.actions.api;

/**
 * Set of commands which constitutes an action.
 * 
 * Important: these commands assumes that current folder is a "scripts" folder,
 * that will be set as a current working directory for the spawned process.
 * 
 * INSTANCE_FOLDER will be available as a environment variable.
 * 
 * @author Sergey Karpushin
 *
 */
public class ActionCommands {
	private String[] invoke;
	private String[] checkStatus;

	private long invocationTimeoutMs = 60000;
	private long statusCheckIntervalMs = 5000;
	private long statusCheckTimeoutMs = 60000;

	public String[] getInvoke() {
		return invoke;
	}

	public void setInvoke(String[] invoke) {
		this.invoke = invoke;
	}

	public String[] getCheckStatus() {
		return checkStatus;
	}

	public void setCheckStatus(String[] checkStatus) {
		this.checkStatus = checkStatus;
	}

	public long getInvocationTimeoutMs() {
		return invocationTimeoutMs;
	}

	public void setInvocationTimeoutMs(long invocationTimeoutMs) {
		this.invocationTimeoutMs = invocationTimeoutMs;
	}

	public long getStatusCheckIntervalMs() {
		return statusCheckIntervalMs;
	}

	public void setStatusCheckIntervalMs(long statusCheckIntervalMs) {
		this.statusCheckIntervalMs = statusCheckIntervalMs;
	}

	public long getStatusCheckTimeoutMs() {
		return statusCheckTimeoutMs;
	}

	public void setStatusCheckTimeoutMs(long statusCheckTimeoutMs) {
		this.statusCheckTimeoutMs = statusCheckTimeoutMs;
	}
}
