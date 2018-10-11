package org.kissfarm.agent.client.api;

import org.summerb.approaches.jdbccrud.common.DtoBase;
import org.summerb.approaches.springmvc.security.dto.LoginParams;

/**
 * {@link #baseUrl} and {@link #authToken} are supposed to be configured.
 * 
 * But {@link #password} is supposed to be retrieved during node registration
 * process
 * 
 * @author Sergey Karpushin
 *
 */
public class ControllerConnectionInfo implements DtoBase {
	private static final long serialVersionUID = 6722682228701501618L;

	private String baseUrl;
	private String authToken;
	private LoginParams loginParams;

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public String getAuthToken() {
		return authToken;
	}

	public void setAuthToken(String authToken) {
		this.authToken = authToken;
	}

	public LoginParams getLoginParams() {
		return loginParams;
	}

	public void setLoginParams(LoginParams loginParams) {
		this.loginParams = loginParams;
	}
}
