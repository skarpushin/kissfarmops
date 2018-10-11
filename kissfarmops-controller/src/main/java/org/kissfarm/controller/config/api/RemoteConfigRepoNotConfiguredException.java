package org.kissfarm.controller.config.api;

import org.summerb.utils.exceptions.GenericException;

public class RemoteConfigRepoNotConfiguredException extends GenericException {
	private static final long serialVersionUID = -3426301901858374666L;

	public RemoteConfigRepoNotConfiguredException() {
		super("repo.exc.RemoteConfigNotConfigured");
	}
}
