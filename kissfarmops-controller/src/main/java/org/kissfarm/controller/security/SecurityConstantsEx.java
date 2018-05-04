package org.kissfarm.controller.security;

import org.summerb.approaches.springmvc.security.SecurityConstants;

public class SecurityConstantsEx extends SecurityConstants {
	/**
	 * @deprecated use it only with Spring security, because it wont accept roles
	 *             prefixed with ROLE_
	 */
	@Deprecated
	public static final String NODE = "NODE";

	public static final String ROLE_NODE = "ROLE_" + NODE;

}
