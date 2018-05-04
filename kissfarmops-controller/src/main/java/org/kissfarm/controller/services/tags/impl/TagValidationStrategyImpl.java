package org.kissfarm.controller.services.tags.impl;

import org.kissfarm.controller.services.tags.api.Tag;
import org.summerb.approaches.jdbccrud.impl.EasyCrudValidationStrategyAbstract;
import org.summerb.approaches.validation.ValidationContext;

public class TagValidationStrategyImpl<TSubjectType> extends EasyCrudValidationStrategyAbstract<Tag<TSubjectType>> {
	private int tagSize = 64;

	public TagValidationStrategyImpl() {
	}

	public TagValidationStrategyImpl(int tagSize) {
		this.tagSize = tagSize;
	}

	@Override
	protected void doValidateForCreate(Tag<TSubjectType> dto, ValidationContext ctx) {
		if (ctx.validateNotEmpty(dto.getTag(), Tag.FN_TAG)) {
			ctx.validateDataLengthLessOrEqual(dto.getTag(), tagSize, Tag.FN_TAG);
		}

		// TODO: Do we need to validate subjectId ?...
	}
}
