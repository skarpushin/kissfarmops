package org.kissfarm.controller.services.nodes.api;

import org.summerb.approaches.jdbccrud.impl.wireTaps.EasyCrudWireTapNoOpImpl;
import org.summerb.approaches.security.api.exceptions.NotAuthorizedException;
import org.summerb.approaches.validation.FieldValidationException;

/**
 * This class will make sure online status will indicate false if record
 * timestamp is before this application instance started
 * 
 * @author Sergey Karpushin
 *
 */
public class NodeStatusOnlinePatchWireTap extends EasyCrudWireTapNoOpImpl<String, NodeStatus> {
	private long appStartedAt = System.currentTimeMillis();

	@Override
	public boolean requiresFullDto() {
		return true;
	}

	@Override
	public boolean requiresOnRead() throws NotAuthorizedException, FieldValidationException {
		return true;
	}

	@Override
	public void afterRead(NodeStatus dto) throws FieldValidationException, NotAuthorizedException {
		if (appStartedAt >= dto.getModifiedAt()) {
			dto.setOnline(false);
		}

		super.afterRead(dto);
	}
}
