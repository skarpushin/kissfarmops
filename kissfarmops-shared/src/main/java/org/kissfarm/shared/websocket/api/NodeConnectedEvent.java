package org.kissfarm.shared.websocket.api;

import org.summerb.approaches.jdbccrud.common.DtoBase;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class NodeConnectedEvent implements DtoBase {
	private static final long serialVersionUID = -3852150126673879841L;

	private String nodeId;
}
