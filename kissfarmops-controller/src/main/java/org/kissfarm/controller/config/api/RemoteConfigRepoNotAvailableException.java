package org.kissfarm.controller.config.api;

import org.summerb.utils.exceptions.GenericException;

public class RemoteConfigRepoNotAvailableException extends GenericException {
	private static final long serialVersionUID = 636216631841416009L;

	public RemoteConfigRepoNotAvailableException(Throwable cause) {
		super("repo.exc.RemoteConfigRepoNotAvailable", cause);
	}

}
