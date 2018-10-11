package org.kissmachine.api.easycrud;

import org.kissmachine.api.dto.SmStateData;
import org.summerb.approaches.jdbccrud.api.EasyCrudService;

public interface SmStateDataService extends EasyCrudService<String, SmStateData> {
	public static final String MESSAGE_CODE = "term.smStateData";

}
