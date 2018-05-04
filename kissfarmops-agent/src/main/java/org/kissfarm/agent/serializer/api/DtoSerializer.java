package org.kissfarm.agent.serializer.api;

import java.io.File;
import java.io.Serializable;

public interface DtoSerializer {
	void save(Object dto, File file);

	<T extends Serializable> T load(File file, Class<T> clazz);
}
