package org.kissfarm.agent.client.api;

import org.summerb.approaches.springmvc.security.dto.LoginParams;

import lombok.Data;

/**
 * {@link #baseUrl} and {@link #authToken} are supposed to be configured.
 * 
 * But {@link #password} is supposed to be retrieved during node registration
 * process
 * 
 * @author Sergey Karpushin
 *
 */
@Data
public class ControllerConnectionInfo {
	private String baseUrl;
	private String authToken;
	private LoginParams loginParams;
}
