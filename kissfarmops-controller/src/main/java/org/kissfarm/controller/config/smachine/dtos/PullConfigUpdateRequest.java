package org.kissfarm.controller.config.smachine.dtos;

import java.io.Serializable;

/**
 * This signal will trigger Farm COnfig state machine to check for new version
 * of the farm config (if in appropriate state of course)
 * 
 * @author sergeyk
 *
 */
public class PullConfigUpdateRequest implements Serializable {
	private static final long serialVersionUID = -3821409266866922514L;

}
