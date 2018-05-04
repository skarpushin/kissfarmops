package org.kissfarm.controller.services.nodes.api;

import org.summerb.approaches.jdbccrud.api.EasyCrudService;

public interface NodeService extends EasyCrudService<String, Node> {
	public static final String TERM = "term.node";

}
