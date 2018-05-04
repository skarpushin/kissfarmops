package org.kissfarm.controller.services.tags.api;

import org.summerb.approaches.jdbccrud.api.dto.HasAuthor;
import org.summerb.approaches.jdbccrud.api.dto.HasAutoincrementId;
import org.summerb.approaches.jdbccrud.api.dto.HasTimestamps;

import lombok.Data;

@Data
public class Tag<TSubjectIdType> implements HasAutoincrementId, HasAuthor, HasTimestamps {
	private static final long serialVersionUID = 2469912795576108890L;

	public static final String FN_SUBJECT_ID = "subjectId";
	public static final String FN_TAG = "tag";

	private Long id;
	private long createdAt;
	private long modifiedAt;
	private String createdBy;
	private String modifiedBy;

	private TSubjectIdType subjectId;
	private String tag;

}
