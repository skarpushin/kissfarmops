package org.kissfarmops.agent.process_execution.api;

import java.util.concurrent.TimeoutException;

/**
 * Interface for communication with started process
 * 
 * @author Sergey Karpushin
 *
 */
public interface ProcessExecution {
	String[] getCommand();

	boolean isStillRunning();

	/**
	 * 
	 * @return process exit code
	 * 
	 * @throws IllegalStateException
	 *             if process wasn't finished yet (i.e. {@link #isStillRunning()}
	 *             returned true
	 */
	int getExitCode();

	void terminate();

	void sendBytesToInput(byte[] bytes);

	/**
	 * Block current thread until process is finished
	 * 
	 * @param timeoutMs
	 *            milliseconds to wait until throw {@link TimeoutException}. 0 means
	 *            wait infinitely
	 * @throws InterruptedException
	 *             if thread was interrupted. Doesn't mean process was finished. You
	 *             can call {@link #isStillRunning()}
	 * @throws TimeoutException
	 *             if timeoutMs elapsed but processed is not finished yet
	 */
	void blockUntilFinished(long timeoutMs) throws InterruptedException, TimeoutException;
}
