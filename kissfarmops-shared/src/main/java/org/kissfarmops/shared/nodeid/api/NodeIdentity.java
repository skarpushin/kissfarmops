package org.kissfarmops.shared.nodeid.api;

import lombok.Data;
import lombok.ToString;

@Data
@ToString(includeFieldNames = true)
public class NodeIdentity {
	/**
	 * Auto-generated by agent
	 */
	private String id;

	/**
	 * Host name
	 */
	private String hostName;

	private String publicIp;

	/**
	 * CSV of tags
	 */
	private String tags;
}
