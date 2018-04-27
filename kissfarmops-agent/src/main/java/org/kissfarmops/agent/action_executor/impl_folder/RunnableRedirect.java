package org.kissfarmops.agent.action_executor.impl_folder;

import java.util.concurrent.atomic.AtomicReference;

public class RunnableRedirect implements Runnable {
	private AtomicReference<Runnable> redirectTo;

	public RunnableRedirect(Runnable redirectTo) {
		this.redirectTo = new AtomicReference<Runnable>(redirectTo);
	}

	public void setRedirectTo(Runnable redirectTo) {
		this.redirectTo.set(redirectTo);
	}

	@Override
	public void run() {
		redirectTo.get().run();
	}
}