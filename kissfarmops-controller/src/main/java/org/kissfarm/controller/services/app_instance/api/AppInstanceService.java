package org.kissfarm.controller.services.app_instance.api;

import org.kissfarm.controller.services.app_instance.dto.AppInstanceRow;
import org.summerb.approaches.jdbccrud.api.EasyCrudService;

public interface AppInstanceService extends EasyCrudService<String, AppInstanceRow> {
	public static final String TERM = "term.appInstance";
}
