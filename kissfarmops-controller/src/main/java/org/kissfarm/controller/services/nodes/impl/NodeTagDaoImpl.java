package org.kissfarm.controller.services.nodes.impl;

import org.kissfarm.controller.services.nodes.api.NodeTag;
import org.kissfarm.controller.services.nodes.api.NodeTagDao;
import org.kissfarm.controller.services.tags.impl.TagDaoImpl;

public class NodeTagDaoImpl extends TagDaoImpl<String, NodeTag> implements NodeTagDao {
	public NodeTagDaoImpl() {
		setDtoClass(NodeTag.class);
	}
}
