package org.kissfarm.controller.services.tags.api;

import org.summerb.approaches.jdbccrud.api.EasyCrudDao;

public interface TagDao<TSubjectIdType, TDtoType extends Tag<TSubjectIdType>> extends EasyCrudDao<Long, TDtoType> {

}
