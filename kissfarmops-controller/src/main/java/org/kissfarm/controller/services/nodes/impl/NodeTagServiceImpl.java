package org.kissfarm.controller.services.nodes.impl;

import org.kissfarm.controller.services.nodes.api.NodeTag;
import org.kissfarm.controller.services.nodes.api.NodeTagDao;
import org.kissfarm.controller.services.nodes.api.NodeTagService;
import org.kissfarm.controller.services.tags.impl.TagServiceImpl;

public class NodeTagServiceImpl extends TagServiceImpl<String, NodeTag, NodeTagDao> implements NodeTagService {
	public NodeTagServiceImpl() {
		setEntityTypeMessageCode(TERM);
		setDtoClass(NodeTag.class);
	}
}
