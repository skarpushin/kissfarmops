package org.kissfarmops.shared.nodeid.api;

import java.util.List;

import lombok.Data;

@Data
public class NodeIdentity {
	/**
	 * Auto-generated by agent
	 */
	private String id;

	/**
	 * List of tags, i.e.e "app:image-server$a app:image-server$b app:host$centos7
	 * custom:xyz"
	 */
	private List<String> tags;

	/**
	 * Host name
	 */
	private String hostName;

	private String publicIp;
}