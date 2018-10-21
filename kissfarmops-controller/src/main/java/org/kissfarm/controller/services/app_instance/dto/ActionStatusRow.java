package org.kissfarm.controller.services.app_instance.dto;

import org.summerb.approaches.jdbccrud.api.dto.HasId;
import org.summerb.approaches.jdbccrud.api.dto.HasTimestamps;
import org.summerb.approaches.jdbccrud.common.DtoBase;

/**
 * Each action execution will be tracked in the database
 * 
 * @author sergeyk
 *
 */
public class ActionStatusRow implements DtoBase, HasId<String>, HasTimestamps {
	private static final long serialVersionUID = 8549999809464253522L;

	private String id;
	/**
	 * Used to group executions together, i.e. when action was executed
	 * simultaneously on multiple nodes
	 */
	private String correlationId;
	private String name;
	private String appId;
	private String params;
	private String result;
	private long createdAt;
	private long modifiedAt;

	@Override
	public String getId() {
		return id;
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public long getCreatedAt() {
		return createdAt;
	}

	@Override
	public void setCreatedAt(long createdAt) {
		this.createdAt = createdAt;
	}

	@Override
	public long getModifiedAt() {
		return modifiedAt;
	}

	@Override
	public void setModifiedAt(long modifiedAt) {
		this.modifiedAt = modifiedAt;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getCorrelationId() {
		return correlationId;
	}

	public void setCorrelationId(String correlationId) {
		this.correlationId = correlationId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}
}
