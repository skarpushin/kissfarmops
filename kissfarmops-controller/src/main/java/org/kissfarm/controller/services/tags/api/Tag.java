package org.kissfarm.controller.services.tags.api;

import org.summerb.approaches.jdbccrud.api.dto.HasAuthor;
import org.summerb.approaches.jdbccrud.api.dto.HasAutoincrementId;
import org.summerb.approaches.jdbccrud.api.dto.HasTimestamps;

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

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
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

	public TSubjectIdType getSubjectId() {
		return subjectId;
	}

	public void setSubjectId(TSubjectIdType subjectId) {
		this.subjectId = subjectId;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

}
