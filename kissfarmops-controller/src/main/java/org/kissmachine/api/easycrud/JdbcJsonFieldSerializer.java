package org.kissmachine.api.easycrud;

import java.io.Serializable;

public interface JdbcJsonFieldSerializer {

	Object serializeObject(Serializable toBeSerialized);

	Serializable deserialize(Serializable data);

}
