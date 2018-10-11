package org.kissfarm.test.tools;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.summerb.microservices.properties.api.SimplePropertyService;
import org.summerb.microservices.properties.api.dto.NamedProperty;

public class SimplePropertyServiceInMemoryImpl implements SimplePropertyService {
	private String appName;
	private String domainName;

	private Map<String, Map<String, String>> props = new HashMap<>();

	public SimplePropertyServiceInMemoryImpl(String appName, String domainName) {
		super();
		this.appName = appName;
		this.domainName = domainName;
	}

	@Override
	public Map<String, String> findSubjectProperties(String subjectId) {
		return props.get(subjectId);
	}

	@Override
	public String findSubjectProperty(String subjectId, String propertyName) {
		Map<String, String> subject = props.get(subjectId);
		if (subject == null) {
			return null;
		}
		return subject.get(propertyName);
	}

	@Override
	public void putSubjectProperties(String subjectId, List<NamedProperty> namedProperties) {
		if (namedProperties == null || namedProperties.size() == 0) {
			return;
		}

		Map<String, String> subject = props.get(subjectId);
		if (subject == null) {
			subject = new HashMap<>();
			props.put(subjectId, subject);
		}

		Map<String, String> subjectF = subject;
		namedProperties.forEach(x -> subjectF.put(x.getName(), x.getPropertyValue()));
	}

	@Override
	public void putSubjectProperty(String subjectId, String propertyName, String propertyValue) {
		Map<String, String> subject = props.get(subjectId);
		if (subject == null) {
			subject = new HashMap<>();
			props.put(subjectId, subject);
		}

		subject.put(propertyName, propertyValue);
	}

	@Override
	public void deleteSubjectProperties(String subjectId) {
		props.remove(subjectId);
	}

	@Override
	public String getAppName() {
		return appName;
	}

	@Override
	public String getDomainName() {
		return domainName;
	}

}
