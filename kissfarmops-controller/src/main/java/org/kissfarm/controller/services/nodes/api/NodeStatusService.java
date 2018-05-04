package org.kissfarm.controller.services.nodes.api;

import org.summerb.approaches.jdbccrud.api.EasyCrudService;

public interface NodeStatusService extends EasyCrudService<String, NodeStatus> {

	public static final String TERM = "term.nodeStatus";

}
