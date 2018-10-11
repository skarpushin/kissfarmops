package org.kissmachine.impl.easycrud;

import org.kissmachine.api.dto.SmData;
import org.kissmachine.api.easycrud.SmDataDao;
import org.kissmachine.api.easycrud.SmDataService;
import org.summerb.approaches.jdbccrud.impl.EasyCrudServicePluggableImpl;

public class SmDataServiceImpl extends EasyCrudServicePluggableImpl<String, SmData, SmDataDao>
		implements SmDataService {
	public SmDataServiceImpl() {
		setDtoClass(SmData.class);
		setEntityTypeMessageCode(MESSAGE_CODE);
	}
}
