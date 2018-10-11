package org.kissfarm.controller.config.api;

import org.summerb.approaches.jdbccrud.common.DtoBase;

public class GitConfig implements DtoBase {
	private static final long serialVersionUID = -8824279008238970287L;

	public static final String FN_URI = "uri";
	public static final String FN_BRANCH = "branch";
	public static final String FN_USER = "user";
	public static final String FN_PASSWORD = "password";

	private String uri;
	private String user;
	// NOTE: It's not ideal. But I leave ssh-based impl for later, don't have to
	// make MVP version perfect
	private String password;
	private String branch = "master";

	public String getUri() {
		return uri;
	}

	public void setUri(String remoteUri) {
		this.uri = remoteUri;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((branch == null) ? 0 : branch.hashCode());
		result = prime * result + ((password == null) ? 0 : password.hashCode());
		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
		result = prime * result + ((user == null) ? 0 : user.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		GitConfig other = (GitConfig) obj;
		if (branch == null) {
			if (other.branch != null) {
				return false;
			}
		} else if (!branch.equals(other.branch)) {
			return false;
		}
		if (password == null) {
			if (other.password != null) {
				return false;
			}
		} else if (!password.equals(other.password)) {
			return false;
		}
		if (uri == null) {
			if (other.uri != null) {
				return false;
			}
		} else if (!uri.equals(other.uri)) {
			return false;
		}
		if (user == null) {
			if (other.user != null) {
				return false;
			}
		} else if (!user.equals(other.user)) {
			return false;
		}
		return true;
	}

}
