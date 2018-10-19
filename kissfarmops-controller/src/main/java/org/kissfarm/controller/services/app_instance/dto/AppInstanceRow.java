package org.kissfarm.controller.services.app_instance.dto;

import org.kissfarm.shared.config.dto.StatusSchema;
import org.summerb.approaches.jdbccrud.api.dto.HasId;
import org.summerb.approaches.jdbccrud.api.dto.HasTimestamps;
import org.summerb.approaches.jdbccrud.common.DtoBase;

/**
 * Database row which will reflect application instance (app name + prototype)
 * 
 * @author sergeyk
 *
 */
public class AppInstanceRow implements DtoBase, HasId<String>, HasTimestamps {
	private static final long serialVersionUID = -5288527448596088534L;

	public static final String FN_NODE_ID = "nodeId";

	private String id;
	private String nodeId;
	private String name;
	private String prototype;
	private StatusSchema statusSchema;
	private String status;
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

	public String getName() {
		return name;
	}

	public void setName(String appName) {
		this.name = appName;
	}

	public String getPrototype() {
		return prototype;
	}

	public void setPrototype(String appPrototype) {
		this.prototype = appPrototype;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
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

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	public StatusSchema getStatusSchema() {
		return statusSchema;
	}

	public void setStatusSchema(StatusSchema statusSchema) {
		this.statusSchema = statusSchema;
	}
}
