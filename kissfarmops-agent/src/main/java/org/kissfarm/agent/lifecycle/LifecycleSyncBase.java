package org.kissfarm.agent.lifecycle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for Lifecycle which will attempt to accomplish task again and
 * again
 * 
 * @author Sergey Karpushin
 *
 */
public abstract class LifecycleSyncBase implements Lifecycle {
	protected Logger log = LoggerFactory.getLogger(getClass());

	private long retryIntervalMs = 10000;

	public LifecycleSyncBase() {
		super();
	}

	@Override
	public Lifecycle call() throws Exception {
		while (true) {
			try {
				return doStep();
			} catch (Throwable t) {
				log.error("Lifecycle step failed. will retry in " + retryIntervalMs + " ms", t);
				try {
					Thread.sleep(retryIntervalMs);
				} catch (InterruptedException ie) {
					log.warn("Received InterruptedException during retry sleep. Exiting.", ie);
					return null;
				}
				continue;
			}
		}
	}

	protected abstract Lifecycle doStep() throws Exception;

	public void setRetryIntervalMs(long retryIntervalMs) {
		this.retryIntervalMs = retryIntervalMs;
	}
}