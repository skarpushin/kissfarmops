package org.kissfarm.agent.process_execution.impl;

import java.io.InputStream;
import java.util.Arrays;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class InputStreamPipeThread extends Thread {
	private Logger log = LoggerFactory.getLogger(ProcessExecutionImpl.class);

	private InputStream inputStream;
	boolean started = false;
	boolean finished = false;

	private Consumer<byte[]> callback;

	public InputStreamPipeThread(String threadName, InputStream inputStream, Consumer<byte[]> callback) {
		super(threadName);
		this.inputStream = inputStream;
		this.callback = callback;
	}

	@Override
	public void run() {
		try {
			started = true;
			byte[] buf = new byte[4096];
			int read;
			while ((read = inputStream.read(buf)) >= 0) {
				byte[] bufToSend = buf;
				if (read < buf.length) {
					bufToSend = Arrays.copyOf(buf, read);
				}

				callback.accept(bufToSend);
			}
		} catch (Throwable e) {
			log.error("Process stream reader died unexpectedly", e);
		} finally {
			safeCloseStream();
			inputStream = null;
			finished = true;
		}
	}

	private void safeCloseStream() {
		try {
			inputStream.close();
		} catch (Throwable t) {
		}
	}
}