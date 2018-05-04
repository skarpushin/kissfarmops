package org.kissfarm.controller.services.nodes.api;

import org.kissfarm.controller.services.tags.api.Tag;

public class NodeTag extends Tag<String> {
	private static final long serialVersionUID = 8625637653235848783L;

	public NodeTag() {
	}

	public NodeTag(String id, String tag) {
		setSubjectId(id);
		setTag(tag);
	}
}
