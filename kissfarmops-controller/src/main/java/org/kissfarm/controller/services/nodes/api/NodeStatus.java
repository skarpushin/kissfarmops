package org.kissfarm.controller.services.nodes.api;

import org.summerb.approaches.jdbccrud.api.dto.HasId;
import org.summerb.approaches.jdbccrud.api.dto.HasTimestamps;

public class NodeStatus implements HasId<String>, HasTimestamps {
	private static final long serialVersionUID = -6455081502460200748L;

	private String id;
	private long createdAt;
	private long modifiedAt;
	private boolean online;

	@Override
	public String getId() {
		return id;
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public long getCreatedAt() {
		return createdAt;
	}

	@Override
	public void setCreatedAt(long createdAt) {
		this.createdAt = createdAt;
	}

	@Override
	public long getModifiedAt() {
		return modifiedAt;
	}

	@Override
	public void setModifiedAt(long modifiedAt) {
		this.modifiedAt = modifiedAt;
	}

	public boolean isOnline() {
		return online;
	}

	public void setOnline(boolean online) {
		this.online = online;
	}

}
