package org.kissfarm.controller.services.nodes.api;

import org.kissfarm.controller.services.tags.api.TagService;

public interface NodeTagService extends TagService<String, NodeTag> {
	public static final String TERM = "term.nodeTag";

}
