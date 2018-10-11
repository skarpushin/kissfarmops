package org.kissmachine.impl.easycrud;

import java.io.Serializable;

import org.kissmachine.api.easycrud.JdbcJsonFieldSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.summerb.approaches.security.api.AuditEvents;
import org.summerb.approaches.security.api.dto.ScalarValue;
import org.summerb.approaches.security.impl.AuditEventsDefaultImpl;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Common methods to (de)serialize objects as JSON into database fields
 * 
 * WARNING/IMPORTANT: Do not use generic types as a root object. Type
 * information could be lost and Gson might choose wrong types for deserialized
 * numbers
 * 
 * @author Sergey Karpushin
 *
 */
public class JdbcJsonFieldSerializerImpl implements JdbcJsonFieldSerializer {
	private Gson gson = new GsonBuilder().create();
	private AuditEvents auditEvents = new AuditEventsDefaultImpl();

	@Override
	public Object serializeObject(Serializable toBeSerialized) {
		if (toBeSerialized == null) {
			return null;
		}

		// NOTE: "Pros": We're not creating another column in the table. "Cons": data
		// from this field coulnd't be used out of the box and will require some basic
		// string manipulations, because it's not a valid JSON.
		String ret = toBeSerialized.getClass().getName() + ":" + gson.toJson(toBeSerialized);
		return ret;
	};

	@Override
	public Serializable deserialize(Serializable data) {
		if (data == null) {
			return null;
		}

		Preconditions.checkArgument(data instanceof String, "Data must be either null or instanceof String");

		try {
			String dataStr = (String) data;
			int separator = dataStr.indexOf(":");
			String className = dataStr.substring(0, separator);
			Class<?> targetClass = Class.forName(className);
			if (!Serializable.class.isAssignableFrom(targetClass)) {
				auditEvents.report(AuditEvents.AUDIT_INJECTION_ATTEMPT, ScalarValue.forV(className));
				throw new IllegalStateException("Not allowed class (possible injection attempt): " + className);
			}
			return (Serializable) gson.fromJson(dataStr.substring(separator + 1), targetClass);
		} catch (Throwable t) {
			throw new RuntimeException("Failed to deserialize SmStateData", t);
		}
	};

	public Gson getGson() {
		return gson;
	}

	@Autowired(required = false)
	public void setGson(Gson gson) {
		this.gson = gson;
	}

	public AuditEvents getAuditEvents() {
		return auditEvents;
	}

	@Autowired(required = false)
	public void setAuditEvents(AuditEvents auditEvents) {
		this.auditEvents = auditEvents;
	}

}
