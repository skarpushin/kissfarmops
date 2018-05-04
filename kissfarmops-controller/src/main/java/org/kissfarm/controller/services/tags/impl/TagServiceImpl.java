package org.kissfarm.controller.services.tags.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.kissfarm.controller.services.tags.api.Tag;
import org.kissfarm.controller.services.tags.api.TagDao;
import org.kissfarm.controller.services.tags.api.TagService;
import org.springframework.transaction.annotation.Transactional;
import org.summerb.approaches.jdbccrud.api.dto.PagerParams;
import org.summerb.approaches.jdbccrud.api.dto.PaginatedList;
import org.summerb.approaches.jdbccrud.api.query.Query;
import org.summerb.approaches.jdbccrud.impl.EasyCrudServicePluggableImpl;
import org.summerb.approaches.security.api.exceptions.NotAuthorizedException;
import org.summerb.approaches.validation.FieldValidationException;

import com.google.common.base.Preconditions;

public class TagServiceImpl<TSubjectIdType, TDtoType extends Tag<TSubjectIdType>, TDaoType extends TagDao<TSubjectIdType, TDtoType>>
		extends EasyCrudServicePluggableImpl<Long, TDtoType, TDaoType> implements TagService<TSubjectIdType, TDtoType> {

	private static final String[] stringArrayType = new String[0];
	private static final Long[] longArrayType = new Long[0];

	@Override
	public List<TSubjectIdType> findSubjectsWithTags(Collection<String> tags) throws NotAuthorizedException {
		Query q = Query.n().in(Tag.FN_TAG, tags.toArray(stringArrayType));
		PaginatedList<TDtoType> results = query(PagerParams.ALL, q);
		if (results.getTotalResults() == 0) {
			return new LinkedList<>();
		}

		// NOTE: I don't like the fact we deduplicate them on the "client". If at some
		// point it'll become a problem we can introduce custom DAO method
		return results.getItems().stream().map(x -> x.getSubjectId()).distinct().collect(Collectors.toList());
	}

	@Override
	@Transactional
	public void setSubjectTags(TSubjectIdType subjectId, Collection<String> tags)
			throws NotAuthorizedException, FieldValidationException {
		Preconditions.checkArgument(subjectId != null, "subjectId is required");
		final Collection<String> newTags = tags != null ? new ArrayList<>(tags) : new LinkedList<>();

		PaginatedList<TDtoType> existing = query(PagerParams.ALL, buildQueryBySubjectId(subjectId));

		if (existing.getTotalResults() > 0) {
			List<Long> idsToDelete = existing.getItems().stream().filter(x -> !newTags.remove(x.getTag()))
					.map(x -> x.getId()).collect(Collectors.toList());
			if (idsToDelete.size() > 0) {
				deleteByQuery(Query.n().in(Tag.FN_ID, idsToDelete.toArray(longArrayType)));
			}
		}

		if (newTags.size() <= 0) {
			return;
		}
		// NOTE: At this point newTags contains only items which are new
		for (String tag : newTags) {
			try {
				TDtoType newRow;
				newRow = getDtoClass().newInstance();
				newRow.setSubjectId(subjectId);
				newRow.setTag(tag);
				create(newRow);
			} catch (Throwable t) {
				throw new RuntimeException("Failed to create new tag: " + tag + " for subject " + subjectId, t);
			}
		}
	}

	@Override
	public void clearSubjectTags(TSubjectIdType subjectId) throws NotAuthorizedException {
		Query q = buildQueryBySubjectId(subjectId);
		deleteByQuery(q);
	}

	private Query buildQueryBySubjectId(TSubjectIdType subjectId) {
		Preconditions.checkArgument(subjectId != null, "subjectId is required");
		Query q;
		if (subjectId instanceof String) {
			q = Query.n().eq(Tag.FN_SUBJECT_ID, (String) subjectId);
		} else if (subjectId instanceof Long) {
			q = Query.n().eq(Tag.FN_SUBJECT_ID, (Long) subjectId);
		} else {
			throw new IllegalArgumentException("This type of subject is not supported: " + subjectId.getClass());
		}
		return q;
	}

}
