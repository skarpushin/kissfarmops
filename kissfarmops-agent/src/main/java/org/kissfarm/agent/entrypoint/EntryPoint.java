package org.kissfarm.agent.entrypoint;

import org.kissfarm.agent.lifecycle.Lifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class EntryPoint {
	public static final Logger log = LoggerFactory.getLogger(EntryPoint.class);

	private static ClassPathXmlApplicationContext currentApplicationContext;

	public static void main(String[] args) {
		Lifecycle firstLifecycle = null;
		try {
			String[] contextPaths = new String[] { "context.xml" };
			currentApplicationContext = new ClassPathXmlApplicationContext(contextPaths);
			currentApplicationContext.registerShutdownHook();
			firstLifecycle = currentApplicationContext.getBean("firstLifecycle", Lifecycle.class);
		} catch (Throwable t) {
			log.error("Unrecoverable failure during agent startup", t);
			System.exit(-1);
			return;
		}

		doLifecycleLoop(firstLifecycle);
	}

	private static void doLifecycleLoop(Lifecycle startingLifecycle) {
		Lifecycle curLifecycle = startingLifecycle;
		log.info("Agent is starting with an initial lifecycle: " + curLifecycle);
		while (curLifecycle != null) {
			Lifecycle next = null;
			try {
				next = curLifecycle.call();
				if (next == null) {
					log.info("Agent is gracefully exiting after lifecycle: {}", curLifecycle);
					break;
				}
				log.info("Agent is switching to lifecycle: {}", next);
			} catch (Throwable t) {
				log.error("Fatal error during lifecycle execution: " + curLifecycle + ". Agent will now die", t);
				break;
				// THINK: Maybe we'd better restart instead of die?
				// Maybe we can register agent as a service which will be automatically
				// restarted when died
			}
			curLifecycle = next;
		}
	}
}
