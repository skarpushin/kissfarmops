package org.kissfarm.controller.services.app_instance.api;

import org.kissfarm.controller.services.app_instance.dto.ActionStatusRow;
import org.summerb.approaches.jdbccrud.api.EasyCrudService;

public interface ActionStatusService extends EasyCrudService<String, ActionStatusRow> {
	public static final String TERM = "term.actionStatus";
}
