package org.kissfarm.controller.websockets.api;

import org.summerb.approaches.jdbccrud.common.DtoBase;

public interface StompOutboundGateway {

	<T extends DtoBase> void sendToUi(T payload);

	<T extends DtoBase> void sendToNode(String nodeId, T payload);

}
