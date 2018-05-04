package org.kissfarm.controller.services.tags.impl;

import org.kissfarm.controller.services.tags.api.Tag;
import org.summerb.approaches.jdbccrud.impl.EasyCrudDaoMySqlImpl;

public class TagDaoImpl<TSubjectIdType, TDtoType extends Tag<TSubjectIdType>>
		extends EasyCrudDaoMySqlImpl<Long, TDtoType> {

}
