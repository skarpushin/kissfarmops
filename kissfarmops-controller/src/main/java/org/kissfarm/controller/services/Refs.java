package org.kissfarm.controller.services;

import org.kissfarm.controller.services.nodes.api.Node;
import org.kissfarm.controller.services.nodes.api.NodeService;
import org.kissfarm.controller.services.nodes.api.NodeStatus;
import org.kissfarm.controller.services.nodes.api.NodeStatusService;
import org.kissfarm.controller.services.nodes.api.NodeTag;
import org.kissfarm.controller.services.nodes.api.NodeTagService;
import org.summerb.approaches.jdbccrud.api.dto.relations.Ref;
import org.summerb.approaches.jdbccrud.api.dto.relations.RefQuantity;
import org.summerb.approaches.jdbccrud.api.dto.relations.RelationType;
import org.summerb.approaches.jdbccrud.impl.relations.ReferencesRegistryPredefinedImpl;

public class Refs extends ReferencesRegistryPredefinedImpl {

	public static Ref nodeTags = new Ref("nodeTags", NodeService.TERM, Node.FN_ID, NodeTagService.TERM,
			NodeTag.FN_SUBJECT_ID, RelationType.Aggregates, RefQuantity.One2Many);

	// NOTE: Although it's One2One, I put One2Many because in this case referenced
	// entity is considered optional
	public static Ref nodeStatus = new Ref("nodeStatus", NodeService.TERM, Node.FN_ID, NodeStatusService.TERM,
			NodeStatus.FN_ID, RelationType.Aggregates, RefQuantity.One2Many);

	public Refs() {
		super(nodeTags, nodeStatus);
	}

}
