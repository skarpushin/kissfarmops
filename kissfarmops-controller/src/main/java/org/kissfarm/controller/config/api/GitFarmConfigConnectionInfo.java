package org.kissfarm.controller.config.api;

import org.summerb.approaches.jdbccrud.common.DtoBase;

public class GitFarmConfigConnectionInfo implements DtoBase {
	private static final long serialVersionUID = -8824279008238970287L;

	private String remoteUri;
	private String user;
	// NOTE: It's not ideal. But I leave ssh-based impl for later, don't have to
	// make MVP version perfect
	private String password;
	private String branch;

	public String getRemoteUri() {
		return remoteUri;
	}

	public void setRemoteUri(String remoteUri) {
		this.remoteUri = remoteUri;
	}

	public String getBranch() {
		return branch;
	}

	public void setBranch(String branch) {
		this.branch = branch;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
