package org.kissfarm.agent.client.impl;

import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.kissfarm.agent.client.api.ControllerConnection;
import org.kissfarm.agent.config.api.ControllerConnectionInfoHolder;
import org.kissfarmops.shared.nodeid.api.NodeIdentity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.codec.Base64;
import org.summerb.approaches.security.api.dto.NotAuthorizedResult;
import org.summerb.approaches.security.api.exceptions.NotAuthorizedException;
import org.summerb.approaches.springmvc.security.dto.LoginParams;
import org.summerb.approaches.springmvc.security.dto.LoginResult;
import org.summerb.approaches.validation.FieldValidationException;
import org.summerb.approaches.validation.ValidationErrors;
import org.summerb.utils.exceptions.ExceptionUtils;
import org.summerb.utils.exceptions.dto.GenericServerErrorResult;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class ControllerConnectionImpl implements ControllerConnection {
	private Logger log = LoggerFactory.getLogger(getClass());

	private static final String DEFAULT_ENCODING = "UTF-8";
	private static final String HEADER_AUTH = "X-Authorization";
	private static final String LOGIN_RELATIVE_PATH = "/rest/login";

	private static List<NameValuePair> commonHeaders = Arrays.asList(
			(NameValuePair) new BasicNameValuePair("Accept", "application/json"),
			new BasicNameValuePair("Content-Type", "application/json"));

	private ControllerConnectionInfoHolder controllerConnectionInfoHolder;

	private Gson gson = new Gson();
	private int socketTimeoutMs = 30000;

	private CookieStore cookieStore = new BasicCookieStore();

	@Override
	public LoginParams register(NodeIdentity nodeIdentity, String authToken) {
		try {
			cookieStore = new BasicCookieStore();
			return post("/rest/api/v1/node-endpoint/register?authToken=" + authToken, nodeIdentity, LoginParams.class);
		} catch (Throwable t) {
			throw new RuntimeException("Registration failed", t);
		}
	}

	@Override
	public void assertLogin(LoginParams loginParams) {
		try {
			cookieStore = new BasicCookieStore();
			List<NameValuePair> headers = new LinkedList<NameValuePair>();
			headers.add(new BasicNameValuePair(HEADER_AUTH, buildBasicAuthHeaderValue(loginParams)));
			LoginResult loginResult;
			loginResult = doGet(LOGIN_RELATIVE_PATH, headers, cookieStore, LoginResult.class);
			if (loginResult == null || loginResult.getUser() == null) {
				throw new RuntimeException("Auth failed, received empty login result");
			}
		} catch (Throwable t) {
			throw new RuntimeException("Failed to login", t);
		}
	}

	@Override
	public String findSessionId() {
		return getCookieStore().getCookies().stream().filter(x -> x.getName().equals("session")).map(x -> x.getValue())
				.findFirst().orElse(null);
	}

	private String baseUrl() {
		return controllerConnectionInfoHolder.getControllerConnectionInfo().getBaseUrl();
	}

	private String buildBasicAuthHeaderValue(LoginParams loginParams) {
		try {
			String tokenBase64Encoded = buildBasicAuthToken(loginParams.getEmail(), loginParams.getPassword());
			return "Basic " + tokenBase64Encoded;
		} catch (Throwable e) {
			throw new RuntimeException("Failed to encode auth parameters", e);
		}
	}

	@SuppressWarnings("deprecation")
	public static String buildBasicAuthToken(String login, String password) throws UnsupportedEncodingException {
		String token = login + ":" + password;
		String tokenBase64Encoded = new String(Base64.encode(token.getBytes("UTF-8")), "UTF-8");
		return tokenBase64Encoded;
	}

	@Override
	public <V, P> V post(String relativeUrl, P query, Class<V> clazz) {
		try {
			V ret = doPost(relativeUrl, gson.toJson(query), null, cookieStore, clazz);
			return ret;
		} catch (Throwable e) {
			throw new RuntimeException("Failed to perform HTTP Post to " + relativeUrl, e);
		}
	}

	@Override
	public String post(String relativeUrl, String requestBody) {
		try {
			String ret = doPostStr(relativeUrl, requestBody, null, cookieStore);
			return ret;
		} catch (Throwable e) {
			throw new RuntimeException("Failed to perform HTTP Post to " + relativeUrl, e);
		}
	}

	private <TResponse> TResponse doGet(String relativeUrl, List<NameValuePair> optionalHeaders,
			CookieStore cookieStore, Class<TResponse> clazz) {
		try {
			String url = baseUrl() + relativeUrl;
			if (log.isTraceEnabled()) {
				log.trace("HTTP GET Request: " + url + ". HTTP Headers: " + buildHttpHeadersLogString(optionalHeaders)
						+ ". HTTP cookies: " + buildHttpCookiesLogString(cookieStore));
			}

			HttpGet httpGet = new HttpGet(url);
			String responseStr = internalDoRequest(optionalHeaders, cookieStore, httpGet);
			return gson.fromJson(responseStr, clazz);
		} catch (Throwable t) {
			throw new RuntimeException("Failed to execute GET request to: " + relativeUrl, t);
		}
	}

	private <TResponse> TResponse doPost(String relativeUrl, String requestText, List<NameValuePair> optionalHeaders,
			CookieStore cookieStore, Class<TResponse> clazz) {
		String responseStr = doPostStr(relativeUrl, requestText, optionalHeaders, cookieStore);
		return gson.fromJson(responseStr, clazz);
	}

	private String doPostStr(String relativeUrl, String requestText, List<NameValuePair> optionalHeaders,
			CookieStore cookieStore) {
		String url = baseUrl() + relativeUrl;
		if (log.isTraceEnabled()) {
			log.trace("HTTP POST Request: " + url + ". HTTP Headers: " + buildHttpHeadersLogString(optionalHeaders)
					+ ". HTTP cookies: " + buildHttpCookiesLogString(cookieStore));
		}

		if (log.isTraceEnabled()) {
			log.trace("HTTP POST Request body: " + requestText);
		}

		HttpPost httpPost = new HttpPost(url);
		httpPost.setEntity(new StringEntity(requestText, DEFAULT_ENCODING));

		String responseStr = internalDoRequest(optionalHeaders, cookieStore, httpPost);
		return responseStr;
	}

	protected String internalDoRequest(List<NameValuePair> optionalHeaders, CookieStore cookieStore,
			HttpUriRequest httpRequest) {
		try {
			addHeaders(httpRequest, commonHeaders);
			if (optionalHeaders != null) {
				addHeaders(httpRequest, optionalHeaders);
			}

			// FYI: http://www.baeldung.com/httpclient-timeout
			RequestConfig config = RequestConfig.custom().setConnectTimeout(socketTimeoutMs)
					.setConnectionRequestTimeout(socketTimeoutMs).setSocketTimeout(socketTimeoutMs).build();
			CloseableHttpClient httpClient = HttpClientBuilder.create().setDefaultCookieStore(cookieStore)
					.setDefaultRequestConfig(config).evictExpiredConnections().build();

			HttpResponse response = httpClient.execute(httpRequest);
			int statusCode = response.getStatusLine().getStatusCode();
			String statusPhrase = response.getStatusLine().getReasonPhrase();
			if (log.isTraceEnabled()) {
				log.trace("HTTP Response " + statusCode + " (" + statusPhrase + "). Response Headers: "
						+ buildHttpHeadersLogString(response.getAllHeaders()));
			}

			HttpEntity entity = response.getEntity();
			String result = entity != null ? EntityUtils.toString(entity, DEFAULT_ENCODING) : "(empty response)";
			if (entity != null) {
				EntityUtils.consume(entity);
			}
			if (log.isTraceEnabled()) {
				log.trace("HTTP Response(" + statusCode + "): " + result);
			}

			throwAppropriateExcBasedOnResponseIfAny(response, statusCode, result);
			return result;
		} catch (Throwable e) {
			if (ExceptionUtils.findExceptionOfType(e, SocketException.class) != null
					|| ExceptionUtils.findExceptionOfType(e, HttpHostConnectException.class) != null
					|| ExceptionUtils.findExceptionOfType(e, UnknownHostException.class) != null) {
				throw new RuntimeException("Connection issue during request", e);
			}
			throw new RuntimeException("Failed to execute request", e);
		}
	}

	protected void throwAppropriateExcBasedOnResponseIfAny(HttpResponse response, int statusCode, String result)
			throws FieldValidationException, NotAuthorizedException {
		if (statusCode == HttpStatus.SC_BAD_REQUEST) {
			try {
				throw new FieldValidationException(gson.fromJson(result, ValidationErrors.class).getErrors());
			} catch (JsonSyntaxException js) {
				if (log.isTraceEnabled()) {
					log.trace("Thought this might be validation error, but failed to parse", js);
				}
				throw new RuntimeException("Unexpected error code: " + statusCode + ", comment: "
						+ response.getStatusLine().getReasonPhrase());
			}
		} else if (statusCode == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
			try {
				String msg = gson.fromJson(result, String.class);
				throw new RuntimeException("EXCEPTION_SERVERSIDE: " + msg);
			} catch (JsonSyntaxException js) {
				try {
					GenericServerErrorResult msg = gson.fromJson(result, GenericServerErrorResult.class);
					if (msg.getExc() == null) {
						throw new RuntimeException("EXCEPTION_SERVERSIDE: " + result);
					}
					throw new RuntimeException("EXCEPTION_SERVERSIDE: " + msg.getExc());
				} catch (JsonSyntaxException js2) {
					throw new RuntimeException("Unexpected error code: " + statusCode + ", comment: "
							+ response.getStatusLine().getReasonPhrase());
				}
			}
		} else if (statusCode == HttpStatus.SC_FORBIDDEN || statusCode == HttpStatus.SC_UNAUTHORIZED) {
			try {
				NotAuthorizedResult msg = gson.fromJson(result, NotAuthorizedResult.class);
				throw new NotAuthorizedException(msg);
			} catch (JsonSyntaxException js) {
				throw new RuntimeException("Unexpected error code: " + statusCode + ", comment: "
						+ response.getStatusLine().getReasonPhrase());
			}
		}
	}

	private void addHeaders(HttpRequest httpRequest, List<NameValuePair> headers) {
		for (NameValuePair pair : headers) {
			httpRequest.addHeader(pair.getName(), pair.getValue());
		}
	}

	private String buildHttpHeadersLogString(Header[] allHeaders) {
		StringBuilder sb = new StringBuilder();
		for (Header pair : allHeaders) {
			sb.append(pair.getName());
			sb.append(" = ");
			sb.append(pair.getValue());
			sb.append(";");
		}
		return sb.toString();
	}

	private String buildHttpHeadersLogString(List<NameValuePair> optionalHeaders) {
		if (isEmpty(optionalHeaders)) {
			return "(none)";
		}

		StringBuilder sb = new StringBuilder();
		for (NameValuePair pair : optionalHeaders) {
			sb.append(pair.getName());
			sb.append(" = ");
			if (HEADER_AUTH.equals(pair.getName())) {
				sb.append("NOT SHOWING PASSWORD");
			} else {
				sb.append(pair.getValue());
			}
			sb.append(";");
		}
		return sb.toString();
	}

	private String buildHttpCookiesLogString(CookieStore cookieStore) {
		if (cookieStore == null || isEmpty(cookieStore.getCookies())) {
			return "(none)";
		}

		StringBuilder sb = new StringBuilder();
		for (Cookie cookie : cookieStore.getCookies()) {
			sb.append(cookie.getName());
			sb.append(" = ");
			sb.append(cookie.getValue());
			sb.append(";");
		}
		return sb.toString();
	}

	@SuppressWarnings("rawtypes")
	private boolean isEmpty(Collection c) {
		return c == null || c.size() == 0;
	}

	public int getSocketTimeoutMs() {
		return socketTimeoutMs;
	}

	public void setSocketTimeoutMs(int socketTimeoutMs) {
		this.socketTimeoutMs = socketTimeoutMs;
	}

	@Override
	public CookieStore getCookieStore() {
		return cookieStore;
	}

	public Gson getGson() {
		return gson;
	}

	@Autowired(required = false)
	public void setGson(Gson gson) {
		this.gson = gson;
	}

	@Autowired
	public void setControllerConnectionInfoHolder(ControllerConnectionInfoHolder controllerConnectionInfoHolder) {
		this.controllerConnectionInfoHolder = controllerConnectionInfoHolder;
	}
}
