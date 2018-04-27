package org.kissfarmops.agent.process_execution.impl;

import java.io.File;
import java.io.OutputStream;
import java.lang.ProcessBuilder.Redirect;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.kissfarmops.agent.process_execution.api.ProcessExecution;
import org.kissfarmops.agent.process_execution.api.ProcessExecutionCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

/**
 * Default impl of {@link ProcessExecution}
 * 
 * @author Sergey Karpushin
 *
 */
public class ProcessExecutionImpl implements ProcessExecution {
	private static Logger log = LoggerFactory.getLogger(ProcessExecutionImpl.class);

	private volatile Process process;
	private ProcessExecutionCallback callback;
	private volatile Integer exitCode;
	private String[] command;
	private InputStreamPipeThread pipeStdout;
	private InputStreamPipeThread pipeStderr;
	private Object processFinishSyncRoot = new Object();
	private OutputStream processInputStream;
	private Thread processWatcherThread;

	public ProcessExecutionImpl(String workingDirectory, Map<String, String> envVariables,
			ProcessExecutionCallback callback, String... paramCommand) {
		this.command = paramCommand;
		this.callback = callback;

		try {
			Preconditions.checkArgument(callback != null, "Callback required");
			Preconditions.checkArgument(command != null && command.length >= 1, "Command required");

			ProcessBuilder builder = new ProcessBuilder(command).redirectError(Redirect.PIPE)
					.redirectOutput(Redirect.PIPE);

			setEnvVariables(envVariables, builder);
			command = replaceEnvVarsInCommand(command, builder.environment());
			builder.command(command); // set it again as we just modified it
			setWorkingDirectory(workingDirectory, builder);

			log.trace("Starting command: {}", Arrays.toString(command));
			process = builder.start();
			processInputStream = process.getOutputStream();
			redirectStreams(command[0], process);
			log.trace("Starting process watcher thread");
			processWatcherThread = new Thread(watcher, command[0]);
			processWatcherThread.start();
			log.trace("Process starting sequence completed");
		} catch (Throwable t) {
			throw new RuntimeException("Failed to start process: " + t.getMessage(), t);
		}
	}

	public static boolean isWindows() {
		return System.getProperty("os.name").toLowerCase().contains("win");
	}

	protected static String[] replaceEnvVarsInCommand(String[] command, Map<String, String> envVariables) {
		String[] ret = command.clone();

		Pattern p = Pattern.compile(isWindows() ? "(\\%[a-zA-Z0-9_]+\\%)" : "(\\$[a-zA-Z0-9_]+)");
		for (int i = 0; i < ret.length; i++) {
			String c = ret[i];
			Matcher m = p.matcher(c);

			while (m.find()) {
				String variable = m.group().replace("$", "").replace("%", "");
				if (variable.length() == 0) {
					continue;
				}

				String varValue = "";
				if (!envVariables.containsKey(variable)) {
					// variable not found
					log.warn("Environment variable wasn't found: " + variable);
				} else {
					varValue = envVariables.get(variable);
				}

				c = c.substring(0, m.start()) + varValue + c.substring(m.end());
				m = p.matcher(c);
			}

			ret[i] = c;
		}

		return ret;
	}

	private void setWorkingDirectory(String workingDirectory, ProcessBuilder builder) {
		if (workingDirectory == null) {
			return;
		}
		builder.directory(new File(workingDirectory));
	}

	private void setEnvVariables(Map<String, String> envVariables, ProcessBuilder builder) {
		if (envVariables == null || envVariables.size() <= 0) {
			return;
		}

		// update ProcessBuilder env vars
		Map<String, String> env = builder.environment();
		envVariables.entrySet().stream().filter(x -> x.getKey() != null && x.getValue() != null)
				.forEach(x -> env.put(x.getKey(), x.getValue()));
	}

	private Runnable watcher = new Runnable() {
		@Override
		public void run() {
			try {
				log.trace("Waiting for process to finish");
				exitCode = process.waitFor();
				log.trace("Process finished, exit code = " + exitCode);

				// TODO: What if something went wrong with Streams?? Now we know process has
				// finished. But onProcessFinished might not stop if streams are not closed

				// wait for streams to drain
				drainStreams();

				log.trace("Notifying callback about finished process, exit code " + exitCode);
				callback.onProcessFinished(exitCode);
			} catch (Throwable t) {
				log.warn("Failed to wait process finish", t);
			} finally {
				processWatcherThread = null;
				process = null;
			}
		}

		private void drainStreams() {
			while (true && !Thread.interrupted()) {
				if (pipeStdout != null && pipeStdout.started && !pipeStdout.finished) {
					continue;
				}
				if (pipeStderr != null && pipeStderr.started && !pipeStderr.finished) {
					continue;
				}
				break;
			}
		}
	};

	@Override
	public void blockUntilFinished(long timeoutMs) throws InterruptedException, TimeoutException {
		Thread t = processWatcherThread;
		if (t == null) {
			log.trace("Can't block thread because proces was already completed");
			return;
		}
		log.trace("Blocking until process is finished");
		t.join(timeoutMs);
		if (t.isAlive()) {
			throw new TimeoutException();
		}
		log.trace("Process finished, not blocking caller anymore");
	}

	private void redirectStreams(String threadBaseName, Process proc) {
		synchronized (processFinishSyncRoot) {
			try {
				pipeStdout = new InputStreamPipeThread(threadBaseName + "|stdout", proc.getInputStream(),
						bytes -> callback.onOutput(bytes));
				pipeStdout.start();

				pipeStderr = new InputStreamPipeThread(command[0] + "|stderr", proc.getErrorStream(),
						bytes -> callback.onError(bytes));
				pipeStderr.start();
				log.trace("Streams redirection configured");
			} catch (Throwable t) {
				throw new RuntimeException("Failed to redirect streams", t);
			}
		}
	}

	@Override
	public void terminate() {
		process.destroy();
	}

	@Override
	public void sendBytesToInput(byte[] bytes) {
		try {
			processInputStream.write(bytes);
			processInputStream.flush();
		} catch (Throwable t) {
			log.warn("Failed to write bytes into process input stream", t);
		}
	}

	@Override
	public String[] getCommand() {
		return command;
	}

	@Override
	public boolean isStillRunning() {
		return process != null;
	}

	@Override
	public int getExitCode() {
		Preconditions.checkState(exitCode != null, "Exit code is unavailable if process is not fiunished yet");
		return exitCode;
	}

}
