package org.kissfarm.controller.config.errors;

import org.summerb.approaches.i18n.HasMessageArgsConverters;
import org.summerb.approaches.i18n.MessageArgConverter;
import org.summerb.approaches.i18n.MessageCodeMessageArgConverter;
import org.summerb.utils.exceptions.GenericException;

public class IncompatibleConfigChangesException extends GenericException implements HasMessageArgsConverters {
	private static final long serialVersionUID = 3468789310952265556L;
	private static final String MESSAGE_CODE = "repo.exc.IncompatibleConfigChanges";

	/**
	 * @param incompatibilityMessageCode - what kind of incompatible change detected
	 * @param subjectId                  some kind of reference to relevant
	 *                                   configuration block. in human readable form
	 */
	public IncompatibleConfigChangesException(String incompatibilityMessageCode, String subjectId) {
		super(MESSAGE_CODE, null, incompatibilityMessageCode, subjectId);
	}

	@Override
	public MessageArgConverter[] getMessageArgsConverters() {
		return new MessageArgConverter[] { MessageCodeMessageArgConverter.INSTANCE, null };
	}

}
