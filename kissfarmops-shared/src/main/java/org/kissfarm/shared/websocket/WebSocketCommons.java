package org.kissfarm.shared.websocket;

import java.util.ArrayList;
import java.util.List;

import org.springframework.messaging.converter.ByteArrayMessageConverter;
import org.springframework.messaging.converter.CompositeMessageConverter;
import org.springframework.messaging.converter.DefaultContentTypeResolver;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.util.MimeTypeUtils;

import com.google.gson.Gson;

public class WebSocketCommons {
	public static final String NTOS = "/ntos";
	public static final String NTOS_REQUESTS = "/node-requests";

	public static String getEndPointRelativePathForServer() {
		return "/ws";
	}

	public static String getEndPointRelativePathForClient() {
		return getEndPointRelativePathForServer() + "/websocket";
	}

	public static String getServerToNodeTopic() {
		return "/ston";
	}

	public static String getServerToNodeTopic(String id) {
		return getServerToNodeTopic() + "/" + id;
	}

	public static String getServerToUiTopic() {
		return "/stoui";
	}

	public static String getNodeToServerBasePath() {
		return NTOS;
	}

	public static String getNodeToServerMapping() {
		return NTOS_REQUESTS;
	}

	public static String getNodeToServerDestination() {
		return NTOS + NTOS_REQUESTS;
	}

	public static String buildServerToNodeTopic(String nodeId) {
		return getServerToNodeTopic() + "/" + nodeId;
	}

	public static MessageConverter getMessageConverter() {
		List<MessageConverter> converters = new ArrayList<MessageConverter>();
		converters.add(new StringMessageConverter());
		converters.add(new ByteArrayMessageConverter());
		converters.add(createJsonConverter());
		return new CompositeMessageConverter(converters);
	}

	private static MessageConverter createJsonConverter() {
		DefaultContentTypeResolver resolver = new DefaultContentTypeResolver();
		resolver.setDefaultMimeType(MimeTypeUtils.APPLICATION_JSON);
		GsonMessageConverter converter = new GsonMessageConverter(new Gson());
		converter.setContentTypeResolver(resolver);
		return converter;
	}

	public static final String ATTR_PAYLOAD_TYPE = "payloadType";

}
