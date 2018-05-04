package org.kissfarm.controller.services.nodes.api;

import org.summerb.approaches.jdbccrud.api.dto.HasId;
import org.summerb.approaches.jdbccrud.api.dto.HasTimestamps;

import lombok.Data;

@Data
public class NodeStatus implements HasId<String>, HasTimestamps {
	private static final long serialVersionUID = -6455081502460200748L;

	private String id;
	private long createdAt;
	private long modifiedAt;
	private boolean online;

}
