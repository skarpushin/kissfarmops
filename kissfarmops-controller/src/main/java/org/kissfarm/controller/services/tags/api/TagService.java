package org.kissfarm.controller.services.tags.api;

import java.util.Collection;
import java.util.List;

import org.summerb.approaches.jdbccrud.api.EasyCrudService;
import org.summerb.approaches.security.api.exceptions.NotAuthorizedException;
import org.summerb.approaches.validation.FieldValidationException;

public interface TagService<TSubjectIdType, TDtoType extends Tag<TSubjectIdType>>
		extends EasyCrudService<Long, TDtoType> {

	List<TSubjectIdType> findSubjectsWithTags(Collection<String> tags) throws NotAuthorizedException;

	void setSubjectTags(TSubjectIdType subjectId, Collection<String> tags)
			throws NotAuthorizedException, FieldValidationException;

	void clearSubjectTags(TSubjectIdType subjectId) throws NotAuthorizedException;

}
