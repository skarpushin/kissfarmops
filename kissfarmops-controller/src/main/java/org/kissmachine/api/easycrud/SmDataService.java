package org.kissmachine.api.easycrud;

import org.kissmachine.api.dto.SmData;
import org.summerb.approaches.jdbccrud.api.EasyCrudService;

public interface SmDataService extends EasyCrudService<String, SmData> {
	public static final String MESSAGE_CODE = "term.smData";

}
