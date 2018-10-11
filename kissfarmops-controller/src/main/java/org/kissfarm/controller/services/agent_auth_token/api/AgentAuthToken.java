package org.kissfarm.controller.services.agent_auth_token.api;

import org.summerb.approaches.jdbccrud.api.dto.HasAuthor;
import org.summerb.approaches.jdbccrud.api.dto.HasTimestamps;
import org.summerb.approaches.jdbccrud.api.dto.HasUuid;

public class AgentAuthToken implements HasUuid, HasAuthor, HasTimestamps {
	private static final long serialVersionUID = 7767256629987403989L;

	private String id;
	private long createdAt;
	private long modifiedAt;
	private String createdBy;
	private String modifiedBy;

	public static final String FN_ENABLED = "enabled";

	public static final String FN_COMMENT = "comment";
	public static final int FN_COMMENT_SIZE = 128;

	private boolean enabled;
	private String comment;

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

	@Override
	public String getCreatedBy() {
		return createdBy;
	}

	@Override
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	@Override
	public String getModifiedBy() {
		return modifiedBy;
	}

	@Override
	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

}
