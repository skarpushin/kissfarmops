package org.kissmachine.impl.easycrud;

import org.kissmachine.api.dto.SmStateData;
import org.kissmachine.api.easycrud.SmStateDataDao;
import org.kissmachine.api.easycrud.SmStateDataService;
import org.summerb.approaches.jdbccrud.impl.EasyCrudServicePluggableImpl;

public class SmStateDataServiceImpl extends EasyCrudServicePluggableImpl<String, SmStateData, SmStateDataDao>
		implements SmStateDataService {
	public SmStateDataServiceImpl() {
		setDtoClass(SmStateData.class);
		setEntityTypeMessageCode(MESSAGE_CODE);
	}
}
