package org.kissfarm.agent.client.api;

import org.apache.http.client.CookieStore;
import org.kissfarm.shared.api.NodeIdentity;
import org.summerb.approaches.springmvc.security.dto.LoginParams;

public interface ControllerConnection {
	<V, P> V post(String relativeUrl, P params, Class<V> clazz);

	String post(String relativeUrl, String requestBody);

	// InputStream openRemoteInputStream(String relativeUrl) throws
	// NotAuthorizedException;

	CookieStore getCookieStore();

	LoginParams register(NodeIdentity nodeIdentity, String authToken);

	void assertLogin(LoginParams loginParams);

	String findSessionId();
}
