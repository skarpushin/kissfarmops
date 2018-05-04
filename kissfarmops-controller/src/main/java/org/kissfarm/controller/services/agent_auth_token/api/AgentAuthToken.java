package org.kissfarm.controller.services.agent_auth_token.api;

import org.summerb.approaches.jdbccrud.api.dto.HasAuthor;
import org.summerb.approaches.jdbccrud.api.dto.HasTimestamps;
import org.summerb.approaches.jdbccrud.api.dto.HasUuid;

import lombok.Data;

@Data
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
}
