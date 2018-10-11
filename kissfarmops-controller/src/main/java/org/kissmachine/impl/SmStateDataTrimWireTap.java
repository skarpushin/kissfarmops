package org.kissmachine.impl;

import org.kissmachine.api.dto.SmStateData;
import org.summerb.approaches.jdbccrud.impl.wireTaps.EasyCrudWireTapNoOpImpl;
import org.summerb.approaches.security.api.exceptions.NotAuthorizedException;
import org.summerb.approaches.validation.FieldValidationException;

/**
 * Simple wire tap to trim all messages before storing it to database
 * 
 * @author sergeyk
 *
 */
public class SmStateDataTrimWireTap extends EasyCrudWireTapNoOpImpl<String, SmStateData> {
	@Override
	public boolean requiresFullDto() {
		return true;
	}

	@Override
	public boolean requiresOnCreate() throws FieldValidationException, NotAuthorizedException {
		return true;
	}

	@Override
	public void beforeCreate(SmStateData dto) throws NotAuthorizedException, FieldValidationException {
		super.beforeCreate(dto);
		dto.setResultMessage(trim(dto.getResultMessage(), 254));
	}

	@Override
	public boolean requiresOnUpdate() throws NotAuthorizedException, FieldValidationException {
		return true;
	}

	@Override
	public void beforeUpdate(SmStateData from, SmStateData to) throws FieldValidationException, NotAuthorizedException {
		super.beforeUpdate(from, to);
		to.setResultMessage(trim(to.getResultMessage(), 254));
	}

	private String trim(String msg, int maxLength) {
		if (msg == null || msg.length() < maxLength) {
			return msg;
		}
		return msg.substring(0, maxLength);
	}
}
