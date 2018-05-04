package org.kissfarm.controller.websockets;

import org.summerb.approaches.jdbccrud.common.DtoBase;

public interface StompGate {

	<T extends DtoBase> void sendToUi(T payload);

	<T extends DtoBase> void sendToNode(String nodeId, T payload);

}
