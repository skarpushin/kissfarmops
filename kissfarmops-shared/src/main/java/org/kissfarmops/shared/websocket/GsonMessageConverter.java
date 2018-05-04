package org.kissfarmops.shared.websocket;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.http.MediaType;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.converter.AbstractMessageConverter;
import org.springframework.messaging.converter.MessageConversionException;
import org.springframework.util.MimeType;
import org.springframework.util.MultiValueMap;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

public class GsonMessageConverter extends AbstractMessageConverter {
	private Logger log = LoggerFactory.getLogger(getClass());

	public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

	private static List<MimeType> mimeTypeList = Arrays.asList(new MediaType("application", "json", DEFAULT_CHARSET),
			new MediaType("application", "*+json", DEFAULT_CHARSET));

	private Gson gson;

	public GsonMessageConverter(Gson gson) {
		super(mimeTypeList);
		this.gson = gson;
	}

	public GsonMessageConverter() {
		super(mimeTypeList);
	}

	@Required
	public void setGson(Gson gson) {
		this.gson = gson;
	}

	public Gson getGson() {
		return gson;
	}

	@Override
	protected boolean supports(Class<?> clazz) {
		return true;
	}

	@Override
	public Object convertFromInternal(Message<?> message, Class<?> targetClass, Object hint) {
		try {
			Object payload = message.getPayload();
			Preconditions.checkArgument(payload != null, "Payload must be not null");

			Reader reader = null;
			if (payload instanceof byte[]) {
				Charset charset = getCharset(getMimeType(message.getHeaders()));
				reader = new InputStreamReader(new ByteArrayInputStream((byte[]) payload), charset);
			} else if (payload instanceof String) {
				reader = new StringReader((String) payload);
			} else {
				throw new IllegalArgumentException("payload of unexpected type cannot be parsed " + payload.getClass());
			}

			Class<?> clarifiedTargetClass = tryClarifyTargetClass(targetClass,
					message.getHeaders().get("nativeHeaders"));

			return this.gson.fromJson(reader, TypeToken.get(clarifiedTargetClass).getType());
		} catch (JsonParseException ex) {
			throw new MessageConversionException(message, "Could not parse JSON", ex);
		} catch (Throwable t) {
			throw new RuntimeException("Failed to parse JSON", t);
		}
	}

	@SuppressWarnings("unchecked")
	private Class<?> tryClarifyTargetClass(Class<?> targetClass, Object nativeHeaders) {
		if (nativeHeaders == null || !(nativeHeaders instanceof MultiValueMap)) {
			return targetClass;
		}

		MultiValueMap<String, String> headers = (MultiValueMap<String, String>) nativeHeaders;
		List<String> payloadTypeList = headers.get(WebSocketCommons.ATTR_PAYLOAD_TYPE);
		if (payloadTypeList == null || payloadTypeList.size() == 0) {
			return targetClass;
		}

		String payloadType = payloadTypeList.get(0);
		try {
			Class<?> clazz = Class.forName(payloadType);
			if (targetClass.isAssignableFrom(clazz)) {
				return clazz;
			} else {
				throw new RuntimeException("Class mismatch. Expected " + targetClass + ", got " + payloadType);
			}
		} catch (Throwable t) {
			log.error("Problem resolving payload type for " + headers, t);
			return targetClass;
		}
	}

	@Override
	public Object convertToInternal(Object payload, MessageHeaders headers, Object hint) {
		try {
			String ret = gson.toJson(payload);
			if (String.class.equals(getSerializedPayloadClass())) {
				return ret;
			}

			if (byte[].class.equals(getSerializedPayloadClass())) {
				return ret.getBytes(getCharset(getMimeType(headers)));
			}

			throw new IllegalArgumentException("Serialization is not supported for " + getSerializedPayloadClass());
		} catch (Throwable ex) {
			throw new MessageConversionException("Could not write JSON: " + ex.getMessage(), ex);
		}
	}

	@Override
	public void setSerializedPayloadClass(Class<?> payloadClass) {
		Preconditions.checkArgument(String.class.equals(payloadClass) || byte[].class.equals(payloadClass),
				"Serialization format is not supported: " + payloadClass);
		super.setSerializedPayloadClass(payloadClass);
	}

	private Charset getCharset(MimeType contentType) {
		if ((contentType != null) && (contentType.getCharset() != null)) {
			return contentType.getCharset();
		}
		return DEFAULT_CHARSET;
	}
}