package org.kissfarm.agent.client.websocket;

import java.lang.reflect.Type;

import org.kissfarm.shared.websocket.WebSocketCommons;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.summerb.approaches.jdbccrud.common.DtoBase;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;

/**
 * Subclass this class and create subscriber methods with parameters of a
 * particular types
 * 
 * <pre>
 * &amp;Subscribe
 * void onDto(Dto dto) {  }
 * </pre>
 * 
 * @author Sergey Karpushin
 *
 */
public class StompFrameHandlerPayloadTypeAware implements StompFrameHandler {
	private Logger log = LoggerFactory.getLogger(getClass());

	private EventBus eventBus;

	public StompFrameHandlerPayloadTypeAware() {
		eventBus = new EventBus(exceptionHandler);
		eventBus.register(this);
	}

	@Override
	public void handleFrame(StompHeaders headers, Object payload) {
		// THINK: What do we do with headers?...

		// NOTE: Subclass expected to have @Subscribe method with parameter of this type
		eventBus.post(payload);
	}

	@Subscribe
	public void onDeadEvent(DeadEvent deadEvent) {
		log.debug("DeadEvent: " + deadEvent.toString());
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Type getPayloadType(StompHeaders headers) {
		try {
			Class clazz = Class.forName(headers.getFirst(WebSocketCommons.ATTR_PAYLOAD_TYPE));
			Preconditions.checkArgument(DtoBase.class.isAssignableFrom(clazz),
					"Message class must implement DtoBase. Possible security threat.");
			return clazz;
		} catch (Throwable t) {
			throw new RuntimeException("Problem resolving payload type for " + headers, t);
		}
	}

	private SubscriberExceptionHandler exceptionHandler = new SubscriberExceptionHandler() {
		@Override
		public void handleException(Throwable exception, SubscriberExceptionContext context) {
			log.warn("Exception during message " + context.getEvent() + " handling. Method = "
					+ context.getSubscriberMethod().getName(), exception);
		}
	};

}