package org.kissfarm.agent.action_executor.impl_folder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.kissfarm.agent.action_executor.api.ActionExecutionSpi;
import org.kissfarm.agent.action_executor.api.ActionInvocationInfo;
import org.kissfarm.agent.action_executor.api.ActionsExecutionListener;
import org.kissfarm.agent.action_executor.api.ResumeMethod;
import org.kissfarm.agent.process_execution.api.ProcessExecution;
import org.kissfarm.agent.process_execution.api.ProcessExecutionCallback;
import org.kissfarm.agent.process_execution.api.ProcessExecutorFactory;
import org.kissfarm.agent.serializer.api.DtoSerializer;
import org.kissfarm.shared.config.dto.ActionCommands;
import org.kissfarm.shared.config.dto.ActionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

/**
 * This impl assumes we're working with regular processes. To invoke an action
 * we spawn a process. And then we might use another process to check status of
 * an action.
 * 
 * If invocation process returned -1 there was an error. If it returned 0 then
 * it means it's completed successfully. If it returns
 * {@value #EXIT_CODE_FOR_ASYNC} it means action is async and we should get back
 * later and check on status using status check command.
 * 
 * This impl will also employ watch dogs to see if either invocation or staus
 * check are timedout.
 * 
 * And it also will be able to suspend operation and resume later. It's
 * implemented for increased resilience.
 * 
 * Under the hood it uses {@link ProcessExecutorFactory} to spawn and monitor
 * child processes.
 * 
 * @author Sergey Karpushin
 */
public class ActionExecutionSpiImpl implements ActionExecutionSpi {
	private static final String FILE_NAME_RESULT = "result";
	private static final String FILE_NAME_INVOCATION_INFO = "z_invocation-info";
	private static final String FILE_NAME_OUTPUT = "z_output.txt";
	private static final String FILE_NAME_LAST_STATUS = "z_last-status";
	private static final String FILE_NAME_RESUME_METHOD_TYPE = "z_resume-method-type";
	private static final String FILE_NAME_RESUME_METHOD = "z_resume-method";

	public static final String ENV_VAR_INSTANCE_FOLDER = "INSTANCE_FOLDER";
	public static final String ENV_VAR_FILE_NAME_INVOCATION_INFO = "INVOCATION_INFO";
	public static final String ENV_VAR_FILE_NAME_RESULT = "RESULT_FILE";

	/**
	 * Special exit code used to indicate this is an async action and it's result is
	 * verified by additional script
	 */
	private static final int EXIT_CODE_FOR_ASYNC = 3;

	private static Logger log = LoggerFactory.getLogger(ActionExecutionSpiImpl.class);

	private static final Charset ENCODING = Charset.forName("UTF-8");

	private ActionsExecutionListener actionsExecutionListener;
	protected ScheduledExecutorService executorService;
	private DtoSerializer dtoSerializer;
	protected ProcessExecutorFactory processExecutorFactory;

	protected ActionInvocationInfo invocationInfo;

	private OutputStream outputStream;
	private StringWriter outputStringWriter;
	protected PrintWriter outputPrintWriter;

	private Object outputSync = new Object();

	private String nameForLog;

	private volatile ActionStatus actionStatus = ActionStatus.Initializing;

	private static final List<ActionStatus> statusesWhenOutputStreamMustBeClosed = Arrays.asList(ActionStatus.Success,
			ActionStatus.Failed, ActionStatus.Exception, ActionStatus.Timedout, ActionStatus.Terminated,
			ActionStatus.Suspended);

	private static final List<ActionStatus> statusesWhenSuspendAvailable = Arrays.asList(ActionStatus.InProgressSync,
			ActionStatus.InProgressAsync);

	private Object lifecycleSync = new Object();

	private volatile ProcessExecution invocation;
	private volatile ScheduledFuture<?> invocationWatchdogFuture;
	private ProcessExecutionCallbackRedirect invocationCallback;
	private RunnableRedirect invocationWatchdog;

	private volatile ScheduledFuture<?> statusCheckScheduledFuture;
	private volatile ProcessExecution statusCheck;
	private volatile ScheduledFuture<?> statusCheckWatchdogFuture;
	private ProcessExecutionCallbackRedirect statusCheckCallback;
	private RunnableRedirect statusCheckWatchdog;

	private ActionExecutionSpiImpl(ActionsExecutionListener actionsExecutionListener, DtoSerializer dtoSerializer) {
		Preconditions.checkArgument(dtoSerializer != null, "dtoSerializer required");
		this.dtoSerializer = dtoSerializer;

		Preconditions.checkArgument(actionsExecutionListener != null, "actionsExecutionListener required");
		this.actionsExecutionListener = actionsExecutionListener;
	}

	private ActionExecutionSpiImpl(ActionsExecutionListener actionsExecutionListener,
			ProcessExecutorFactory processExecutorFactory, ScheduledExecutorService executorService,
			DtoSerializer dtoSerializer) {
		injectDependencies(actionsExecutionListener, processExecutorFactory, executorService, dtoSerializer);

		invocationCallback = new ProcessExecutionCallbackRedirect(invocationCallbackActual);
		invocationWatchdog = new RunnableRedirect(invocationWatchdogActual);

		statusCheckCallback = new ProcessExecutionCallbackRedirect(statusCheckCallbackActual);
		statusCheckWatchdog = new RunnableRedirect(statusCheckWatchdogActual);
	}

	/**
	 * Constructor for a case when action is just being invoked
	 * 
	 * @param invocationInfo
	 *            information for action invocation that can be serialized.
	 *            IMPORTANT: It's assumed that instance folder is created by caller
	 */
	public static ActionExecutionSpi startNewInvocation(ActionInvocationInfo invocationInfo,
			DtoSerializer dtoSerializer, ScheduledExecutorService executorService,
			ProcessExecutorFactory processExecutorFactory, ActionsExecutionListener actionsExecutionListener) {

		ActionExecutionSpiImpl r = new ActionExecutionSpiImpl(actionsExecutionListener, processExecutorFactory,
				executorService, dtoSerializer);

		try {
			r.validateAndSetActionInvocationInfo(invocationInfo);
			dtoSerializer.save(invocationInfo, r.getInvocationInfoFile());

			r.initNewOutputStream();

			// IMPORTANT !!!!!!!!!!!!!!!!!!!!!!!!
			// If you change this code also check out StartPostponedActionDeferedHandler,
			// because there is slight duplication between here and there.

			// not set status and spawn a process
			r.setActionStatus(ActionStatus.InProgressSync);

			Map<String, String> enrichedEnvVars = r.enrichEnvVarsBeforeScriptExecution();
			r.invokeAction(invocationInfo.getScriptsFolder(), invocationInfo.getActionCommands(), executorService,
					processExecutorFactory, enrichedEnvVars);
		} catch (Throwable t) {
			r.logExceptionToBoth("Exception happened while invoking an action " + r.toStringForLog(), t);
			r.setActionStatus(ActionStatus.Exception);
		}
		return r;
	}

	/**
	 * Action will be prepared for the reconciliation right from the very beginning.
	 * It won't be started right away. Only invocation info will be saved
	 */
	public static ActionExecutionSpi postponeAction(ActionInvocationInfo invocationInfo, DtoSerializer dtoSerializer,
			ActionsExecutionListener actionsExecutionListener) {

		ActionExecutionSpiImpl ret = new ActionExecutionSpiImpl(actionsExecutionListener, dtoSerializer);
		ret.validateAndSetActionInvocationInfo(invocationInfo);
		dtoSerializer.save(invocationInfo, ret.getInvocationInfoFile());

		ret.setResumeMethodUnsafe(new StartPostponedActionDeferedHandler(), "PostponedAction");
		ret.setActionStatus(ActionStatus.Postponed);
		return ret;
	}

	private void injectDependencies(ActionsExecutionListener actionsExecutionListener,
			ProcessExecutorFactory processExecutorFactory, ScheduledExecutorService executorService,
			DtoSerializer dtoSerializer) {
		Preconditions.checkArgument(processExecutorFactory != null, "processExecutorFactory required");
		this.processExecutorFactory = processExecutorFactory;

		Preconditions.checkArgument(dtoSerializer != null, "dtoSerializer required");
		this.dtoSerializer = dtoSerializer;

		Preconditions.checkArgument(actionsExecutionListener != null, "actionsExecutionListener required");
		this.actionsExecutionListener = actionsExecutionListener;

		this.executorService = executorService;
		Preconditions.checkArgument(executorService != null, "executorService required");
	}

	private void validateAndSetActionInvocationInfo(ActionInvocationInfo invocationInfo) {
		this.invocationInfo = invocationInfo;

		Preconditions.checkArgument(invocationInfo != null, "invocationInfo required");
		Preconditions.checkArgument(StringUtils.isNotEmpty(invocationInfo.getScriptsFolder()),
				"scriptsFolder var must not be empty");
		Preconditions.checkArgument(new File(invocationInfo.getScriptsFolder()).exists(), "scriptsFolder must exist");
		Preconditions.checkArgument(StringUtils.isNotEmpty(invocationInfo.getInstanceFolder()),
				"instanceFolder var must not be empty");
		Preconditions.checkArgument(new File(invocationInfo.getInstanceFolder()).exists(), "instanceFolder must exist");
		Preconditions.checkArgument(invocationInfo.getActionCommands() != null, "actionCommands info required");
	}

	protected void invokeAction(String scriptsFolder, ActionCommands actionCommands,
			ScheduledExecutorService executorService, ProcessExecutorFactory processExecutorFactory,
			Map<String, String> enrichedEnvVars) {
		log.trace("Starting timeout watchdog for {}", toStringForLog());
		invocationWatchdogFuture = executorService.schedule(invocationWatchdog, actionCommands.getInvocationTimeoutMs(),
				TimeUnit.MILLISECONDS);
		try {
			log.trace("Invoking {}", toStringForLog());
			invocation = processExecutorFactory.execute(scriptsFolder, enrichedEnvVars, invocationCallback,
					invocationInfo.getActionCommands().getInvoke());
		} catch (Throwable t) {
			invocationWatchdogFuture = null;
			throw new RuntimeException("Failed to invoke action: " + toStringForLog(), t);
		}
	}

	protected Map<String, String> enrichEnvVarsBeforeScriptExecution() {
		Map<String, String> enrichedEnvVars = new HashMap<>();
		if (invocationInfo.getEnvVars() != null) {
			enrichedEnvVars.putAll(invocationInfo.getEnvVars());
		}
		enrichedEnvVars.put(ENV_VAR_INSTANCE_FOLDER, invocationInfo.getInstanceFolder());
		enrichedEnvVars.put(ENV_VAR_FILE_NAME_INVOCATION_INFO, getInvocationInfoFile().getAbsolutePath());
		enrichedEnvVars.put(ENV_VAR_FILE_NAME_RESULT, getResultFile().getAbsolutePath());
		return enrichedEnvVars;
	}

	private void initNewOutputStream() {
		try {
			outputStream = new FileOutputStream(getOutputFile(), false);
			outputStringWriter = new StringWriter();
			outputPrintWriter = new PrintWriter(outputStringWriter);
		} catch (Throwable t) {
			throw new RuntimeException("Failed to init new outout stream for the action " + invocationInfo.getName(),
					t);
		}
	}

	private void initOutputStreamForReconciliation() {
		try {
			Preconditions.checkState(invocationInfo != null,
					"invocationInfo is supposed to be initialized by the time this method is called");

			outputStream = new FileOutputStream(getOutputFile(), true);
			outputStringWriter = new StringWriter();

			String currentOutput = getOutput();
			if (currentOutput != null) {
				outputStringWriter.append(currentOutput);
			}

			outputPrintWriter = new PrintWriter(outputStringWriter);
		} catch (Throwable t) {
			outputStream = null;
			outputStringWriter = null;
			outputPrintWriter = null;

			throw new RuntimeException(
					"Failed to init existing outout stream for the action " + invocationInfo.getName(), t);
		}
	}

	protected void appendToOutput(String str) {
		appendToOutput(str.getBytes(ENCODING));
	}

	private void appendToOutput(byte[] newBytes) {
		if (newBytes == null || newBytes.length == 0) {
			return; // as nothing to write
		}

		try {
			synchronized (outputSync) {
				Preconditions.checkState(outputStringWriter != null,
						"Output buffer is expected to be available, but it's not");
				Preconditions.checkState(outputStream != null,
						"Output stream is expected to be available, but it's not");

				// NOTE: We intentionally write to disk first. If app fails at least we'll be
				// eable to reconcile data from disk
				outputStream.write(newBytes);
				outputStringWriter.append(new String(newBytes, ENCODING));
			}
		} catch (Throwable t) {
			log.error("Failed to write data to the output. Data length: " + newBytes + ". Bytes: "
					+ Arrays.toString(newBytes), t);
			// NOTE: Not going to re-throw
		}
	}

	private Runnable invocationWatchdogActual = new Runnable() {
		@Override
		public void run() {
			synchronized (lifecycleSync) {
				if (!isCanHandleInvocationTimeout()) {
					return;
				}

				handleInvocationTimeOutWatchdog();
				setActionStatus(ActionStatus.Timedout);
			}
		}
	};

	protected Runnable invocationWatchdogSuspending = new Runnable() {
		@Override
		public void run() {
			synchronized (lifecycleSync) {
				if (!isCanHandleInvocationTimeout()) {
					return;
				}

				if (!setResumeMethod(new InvocationTimeOutDeferedHandler(), "InvocationTimeOut")) {
					return;
				}
				setActionStatus(ActionStatus.Suspended);
			}
		}
	};

	private void handleInvocationTimeOutWatchdog() {
		// NOTE: We assuming invocation is not null, because all modifications
		// to this variable must be performed within critical section
		// lifecycleSync
		try {
			appendToOutput("Action invocation " + toStringForLog() + " will be terminated due to a timeout\r\n");
			invocation.terminate();
		} catch (Throwable t) {
			this.logExceptionToBoth(
					"Failed to terminate action " + toStringForLog() + " on timeout due to unexpected error", t);
		}
		invocation = null;
	}

	private boolean isCanHandleInvocationTimeout() {
		if (invocationWatchdogFuture == null) {
			// this case means that invocationCallback completed normally while we
			// were entering this critical section
			log.trace("Timeout watchdog invoked for {}, but invocation seemed to be already completed",
					toStringForLog());
			return false;
		}
		log.trace("Timeout watchdog invoked for {}, terminating", toStringForLog());
		invocationWatchdogFuture = null;
		return true;
	}

	private ProcessExecutionCallback invocationCallbackActual = new ProcessExecutionCallback() {
		@Override
		public void onProcessFinished(int exitCode) {
			synchronized (lifecycleSync) {
				if (!isCanHandleInvocationExitCode()) {
					return;
				}

				handleInvocationExitCode(exitCode);
			}
		}

		@Override
		public void onOutput(byte[] output) {
			appendToOutput(output);
		}

		@Override
		public void onError(byte[] error) {
			appendToOutput(error);
		}
	};

	protected ProcessExecutionCallback invocationCallbackSuspending = new ProcessExecutionCallback() {
		@Override
		public void onProcessFinished(int exitCode) {
			synchronized (lifecycleSync) {
				if (!isCanHandleInvocationExitCode()) {
					return;
				}

				if (!setResumeMethod(new InvocationExitCodeDeferedHandler(exitCode), "InvocationExitCode")) {
					return;
				}
				setActionStatus(ActionStatus.Suspended);
			}
		}

		@Override
		public void onOutput(byte[] output) {
			appendToOutput(output);
		}

		@Override
		public void onError(byte[] error) {
			appendToOutput(error);
		}
	};

	private boolean isCanHandleInvocationExitCode() {
		if (invocationWatchdogFuture == null) {
			// it means action was timed out, so we shouldn't be processing the result
			// actually it supposed to be terminated
			log.warn(
					"Action {} invocation was terminated, but we still received notification about it's completion. It might result in inconsistent system state",
					toStringForLog());
			return false;
		}

		// cancel timeout watchdog
		log.trace("Canceling invocation timeout watchdog for {}", toStringForLog());
		invocationWatchdogFuture.cancel(true);
		invocationWatchdogFuture = null;
		invocation = null;
		return true;
	}

	protected void handleInvocationExitCode(int exitCode) {
		if (isFailureExitCode(exitCode)) {
			setActionStatus(ActionStatus.Failed);
			return;
		}

		if (isSuccessExitCode(exitCode)) {
			setActionStatus(ActionStatus.Success);
			return;
		}

		// verify we have means of checking async action status
		boolean hasActionStatusCheckCommand = invocationInfo.getActionCommands().getCheckStatus() != null
				&& invocationInfo.getActionCommands().getCheckStatus().length > 0;
		if (!hasActionStatusCheckCommand) {
			appendToOutput("Action " + toStringForLog() + " invocation command returned positive value " + exitCode
					+ ", which means it's an async operation, but there is no command to check for status\r\n");
			setActionStatus(ActionStatus.Exception);
			return;
		}

		log.trace("Action {} seem to be of a async nature. Scheduling status check routine", toStringForLog());
		scheduleStatusCheck();
	}

	/**
	 * This thing will invoke status checking command to check command status
	 */
	protected Runnable statusCheckWorker = new Runnable() {
		@Override
		public void run() {
			synchronized (lifecycleSync) {
				if (actionStatus != ActionStatus.InProgressAsync) {
					return;
				}

				// TBD: Put a comment here, explaining WHY we doing this thing here with
				// statusCheckScheduledFuture
				if (statusCheckScheduledFuture == null) {
					return;
				}
				statusCheckScheduledFuture = null;

				log.trace("It's time to check status of {}", toStringForLog());
				Map<String, String> enrichedEnvVars = enrichEnvVarsBeforeScriptExecution();
				invokeStatusCheck(enrichedEnvVars);
			}
		}
	};

	private void invokeStatusCheck(Map<String, String> enrichedEnvVars) {
		log.trace("Starting timeout watchdog for status of {}", toStringForLog());
		statusCheckWatchdogFuture = executorService.schedule(statusCheckWatchdog,
				invocationInfo.getActionCommands().getStatusCheckTimeoutMs(), TimeUnit.MILLISECONDS);

		try {
			log.trace("Invoking status check for {}", toStringForLog());
			statusCheck = processExecutorFactory.execute(invocationInfo.getScriptsFolder(), enrichedEnvVars,
					statusCheckCallback, invocationInfo.getActionCommands().getCheckStatus());
		} catch (Throwable t) {
			statusCheckWatchdogFuture = null;
			log.error("Failed to invoke status check on task " + toStringForLog(), t);

			appendToOutput("Failed to invoke status check on task " + toStringForLog()
					+ ". Task will be reported as failed\r\n");
			t.printStackTrace(outputPrintWriter);

			setActionStatus(ActionStatus.Exception);
			// throw new RuntimeException("Failed to invoke action status check: " +
			// toStringForLog(), t);
		}
	}

	/**
	 * This will be invoked by executorService if timeout expired
	 */
	protected Runnable statusCheckWatchdogActual = new Runnable() {
		@Override
		public void run() {
			synchronized (lifecycleSync) {
				if (!isStatusCheckWatchdogCanProceed()) {
					return;
				}

				handleStatusCheckTimedOutWatchdog();
				setActionStatus(ActionStatus.Timedout);
			}
		}
	};

	protected Runnable statusCheckWatchdogSuspending = new Runnable() {
		@Override
		public void run() {
			synchronized (lifecycleSync) {
				if (!isStatusCheckWatchdogCanProceed()) {
					return;
				}

				handleStatusCheckTimedOutWatchdog();
				if (!setResumeMethod(new StatusCheckTimedOutDeferedHandler(), "StatusCheckTimeOut")) {
					return;
				}
				setActionStatus(ActionStatus.Suspended);
			}
		}
	};

	private boolean isStatusCheckWatchdogCanProceed() {
		if (statusCheckWatchdogFuture == null) {
			log.trace("Timeout watchdog invoked for status of {}, but invocation seemed to be already completed",
					toStringForLog());
			return false;
		}
		log.trace("Timeout watchdog invoked for status of {}, terminating", toStringForLog());
		statusCheckWatchdogFuture = null;
		return true;
	}

	private void handleStatusCheckTimedOutWatchdog() {
		try {
			appendToOutput("Action status invocation " + toStringForLog() + " will be terminated due to a timeout\r\n");
			statusCheck.terminate();
		} catch (Throwable t) {
			this.logExceptionToBoth(
					"Failed to terminate status check of " + toStringForLog() + " on timeout due to unexpected error",
					t);
		}
		statusCheck = null;
	}

	/**
	 * Callback used in normal operation
	 */
	protected ProcessExecutionCallback statusCheckCallbackActual = new ProcessExecutionCallback() {
		@Override
		public void onProcessFinished(int exitCode) {
			synchronized (lifecycleSync) {
				if (!isStatusCheckHandlerCanProceed()) {
					return;
				}
				handleStatusCheckExitCode(exitCode);
			}
		}

		@Override
		public void onOutput(byte[] output) {
			appendToOutput(output);
		}

		@Override
		public void onError(byte[] error) {
			appendToOutput(error);
		}
	};

	private boolean isStatusCheckHandlerCanProceed() {
		// WARNING: This is NOT idempotent operation
		if (statusCheckWatchdogFuture == null) {
			log.warn(
					"Status check of {} was terminated, but we still received notification about it's completion. It might result in inconsistent system state",
					toStringForLog());
			return false;
		}

		// cancel timeout watchdog
		log.trace("Canceling status check timeout watchdog for {}", toStringForLog());
		statusCheckWatchdogFuture.cancel(true);
		statusCheckWatchdogFuture = null;
		statusCheck = null;
		return true;
	}

	protected void handleStatusCheckExitCode(int exitCode) {
		if (isFailureExitCode(exitCode)) {
			setActionStatus(ActionStatus.Failed);
		} else if (isSuccessExitCode(exitCode)) {
			setActionStatus(ActionStatus.Success);
		} else {
			log.trace("Status of action {} = check back later. Scheduling status check routine", toStringForLog());
			scheduleStatusCheck();
		}
	}

	protected void scheduleStatusCheck() {
		setActionStatus(ActionStatus.InProgressAsync);
		statusCheckScheduledFuture = executorService.schedule(statusCheckWorker,
				invocationInfo.getActionCommands().getStatusCheckIntervalMs(), TimeUnit.MILLISECONDS);
	}

	/**
	 * Callback used when action is being suspended
	 */
	protected ProcessExecutionCallback statusCheckCallbackSuspending = new ProcessExecutionCallback() {
		@Override
		public void onProcessFinished(int exitCode) {
			synchronized (lifecycleSync) {
				if (!isStatusCheckHandlerCanProceed()) {
					return;
				}

				if (!setResumeMethod(new StatusCheckExitCodeDeferedHandler(exitCode), "StatusCheckExitCode")) {
					return;
				}
				setActionStatus(ActionStatus.Suspended);
			}
		}

		@Override
		public void onOutput(byte[] output) {
			appendToOutput(output);
		}

		@Override
		public void onError(byte[] error) {
			appendToOutput(error);
		}
	};

	/**
	 * @return true if ok, false if exception. This method is used when we got
	 *         called via callback from downstream components and there is no reason
	 *         to propagate exceptions. Use
	 *         {@link #setResumeMethodUnsafe(ResumeMethod, String)} if exception
	 *         propagation is ok
	 */
	private boolean setResumeMethod(ResumeMethod resumeMethod, String actionPhase) {
		try {
			setResumeMethodUnsafe(resumeMethod, actionPhase);
			return true;
		} catch (Throwable t) {
			this.logExceptionToBoth("Failed to setResumeMethod", t);
			return false;
		}
	}

	private void setResumeMethodUnsafe(ResumeMethod resumeMethod, String actionPhase) {
		try {
			dtoSerializer.save(resumeMethod, getFileResumeMethod());
			FileUtils.write(getFileResumeMethodType(), resumeMethod.getClass().getName(), ENCODING, false);
		} catch (Throwable t) {
			setActionStatus(ActionStatus.Exception);
			throw new RuntimeException("Failed to set resume method (to pick up after reconciliation) for action "
					+ toStringForLog() + " and it's phase " + actionPhase, t);
		}
	}

	/**
	 * Log exception to both streams - our log file and action's output
	 */
	protected void logExceptionToBoth(String msg, Throwable t) {
		log.error(msg, t);

		if (outputStream != null) {
			appendToOutput(msg + "\r\n");
			t.printStackTrace(outputPrintWriter);
		}
	}

	private ResumeMethod tryLoadResumeMethod() {
		try {
			if (!getFileResumeMethodType().exists() || !getFileResumeMethod().exists()) {
				return null;
			}

			Class<ResumeMethod> resumeMethodClass = getResumeMethodClass();
			ResumeMethod resumeMethod = dtoSerializer.load(getFileResumeMethod(), resumeMethodClass);

			return resumeMethod;
		} catch (Throwable t) {
			throw new RuntimeException("Failed to load resume method", t);
		}
	}

	private Class<ResumeMethod> getResumeMethodClass() throws IOException, ClassNotFoundException {
		String resumeMethodClassName = FileUtils.readFileToString(getFileResumeMethodType(), ENCODING);

		// WARNING: SECURITY: Possible vulnerability, might need to better restrict
		// classes that we can load this way
		Class<?> resumeMethodClassRaw = Class.forName(resumeMethodClassName);
		Preconditions.checkState(ResumeMethod.class.isAssignableFrom(resumeMethodClassRaw),
				"Incompatible type. Must be a subclass of ResumeMethod");
		@SuppressWarnings("unchecked") // static code analysis failed to see it was checked
		Class<ResumeMethod> resumeMethodClass = (Class<ResumeMethod>) resumeMethodClassRaw;
		return resumeMethodClass;
	}

	@Override
	public void suspend() {
		try {
			Preconditions.checkState(statusesWhenSuspendAvailable.contains(actionStatus),
					"Action cannot be suspended when in this status: " + actionStatus);

			// Suspend case when delay between status checks -- in most times this should be
			// the case when we're suspending
			synchronized (lifecycleSync) {
				if (statusCheckScheduledFuture != null) {
					statusCheckScheduledFuture.cancel(true);
					statusCheckScheduledFuture = null;

					setResumeMethodUnsafe(new ResumeStatusChecksDeferedHandler(), "ResumeStatusChecks");
					setActionStatus(ActionStatus.Suspended);
					return;
				}
			}

			// Case 2: we're trying to suspend when when are in the middle of the process
			// execution
			// Just replace all of them, regardless of which one is actually the case
			invocationCallback.setRedirectTo(invocationCallbackSuspending);
			invocationWatchdog.setRedirectTo(invocationWatchdogSuspending);
			statusCheckCallback.setRedirectTo(statusCheckCallbackSuspending);
			statusCheckWatchdog.setRedirectTo(statusCheckWatchdogSuspending);
		} catch (Throwable t) {
			throw new RuntimeException("Failed to suspend " + toStringForLog(), t);
		}
	}

	/**
	 * Constructor for a case when action is reconciled from an instance folder
	 * 
	 * @param instanceFolder
	 *            working folder where action intermediate state will be stored
	 * @param executorService
	 *            service used to handle async actions like starting a process,
	 *            redirecting inputStreams
	 * @param actionsExecutionListener
	 *            handler of the action status change events
	 * @param scriptsFolder
	 *            scripts folder location
	 */
	public static ActionExecutionSpiImpl reconcileExistingAction(String instanceFolder, DtoSerializer dtoSerializer,
			ScheduledExecutorService executorService, ProcessExecutorFactory processExecutorFactory,
			ActionsExecutionListener actionsExecutionListener) {

		ActionExecutionSpiImpl r = null;

		try {
			log.trace("Starting reconciliation of action from instance folder: " + instanceFolder);
			Preconditions.checkState(new File(instanceFolder).exists(), "Instance folder %s doesn't exist",
					instanceFolder);
			ActionStatus loadLastRecordedActionStatus = loadLastRecordedActionStatus(instanceFolder);
			Preconditions.checkState(isStatusEligibleForReconciliation(loadLastRecordedActionStatus),
					"It appears action wasn't interrupted gracefully. Only Postponed or Suspended actions can be reconciled");

			r = new ActionExecutionSpiImpl(actionsExecutionListener, processExecutorFactory, executorService,
					dtoSerializer);
			r.invocationInfo = dtoSerializer.load(getInvocationInfoFile(instanceFolder), ActionInvocationInfo.class);
			r.actionStatus = loadLastRecordedActionStatus;
			r.initOutputStreamForReconciliation();

			// was it a graceful suspension -OR- "violent"? If we can load resumeMethod -
			// it means it was graceful
			ResumeMethod resumeMethod = r.tryLoadResumeMethod();
			if (resumeMethod != null) {
				deleteResumeMethodFiles(r);
				resumeMethod.setImpl(r);
				log.trace("OK, resume method was loaded and now we'll invoke it: {}", resumeMethod);
				resumeMethod.run();
			} else {
				// well that's a problem. Shutdown was not graceful
				throw new IllegalStateException(
						"Failed to reconcile action that was interrupted in a non-graceful way, ResumeMethod is not recorded");
			}

			return r;
		} catch (Throwable t) {
			String msg = "Failed to reconcile ActionExecutionSpiImpl from instance folder: " + instanceFolder;
			r.logExceptionToBoth(msg, t);
			r.setActionStatus(ActionStatus.Exception);
			throw new RuntimeException(msg, t);
		}
	}

	private static boolean isStatusEligibleForReconciliation(ActionStatus loadLastRecordedActionStatus) {
		return loadLastRecordedActionStatus == ActionStatus.Postponed
				|| loadLastRecordedActionStatus == ActionStatus.Suspended;
	}

	private static void deleteResumeMethodFiles(ActionExecutionSpiImpl r) throws IOException {
		try {
			FileUtils.forceDelete(r.getFileResumeMethodType());
			FileUtils.forceDelete(r.getFileResumeMethod());
		} catch (Throwable t) {
			throw new RuntimeException(
					"Failed to delete files used to describe resume method. Hence we cant guarantee resilient opeartion. Resume method file: "
							+ r.getFileResumeMethod(),
					t);
		}
	}

	protected boolean isSuccessExitCode(int exitCode) {
		// NOTE: it matches exit code of true. I.e. run: sh -c "true" ; echo $?
		return exitCode == 0;
	}

	protected boolean isFailureExitCode(int exitCode) {
		// NOTE: it matches exit code of false. I.e. run: sh -c "false" ; echo $?
		return exitCode > 0 && exitCode != EXIT_CODE_FOR_ASYNC;
	}

	@Override
	public String toString() {
		return toStringForLog();
	}

	public String toStringForLog() {
		if (invocationInfo == null) {
			return super.toString();
		}

		if (nameForLog == null) {
			nameForLog = invocationInfo.getName() + "@" + invocationInfo.getExecutionId();
		}
		return nameForLog;
	}

	@Override
	public String getName() {
		return invocationInfo.getName();
	}

	@Override
	public String getExecutionId() {
		return invocationInfo.getExecutionId();
	}

	@Override
	public ActionStatus getStatus() {
		return actionStatus;
	}

	@Override
	public String getResult() {
		if (!getResultFile().exists()) {
			return null;
		}

		try {
			return FileUtils.readFileToString(getResultFile(), ENCODING).trim();
		} catch (Throwable t) {
			throw new RuntimeException("Failed to read action result", t);
		}
	}

	@Override
	public String getOutput() {
		StringWriter tempOutput = outputStringWriter;
		if (tempOutput != null) {
			return tempOutput.toString().trim();
		}

		if (!getOutputFile().exists()) {
			return null;
		}

		try {
			return FileUtils.readFileToString(getOutputFile(), ENCODING).trim();
			// NOTE: Should we read it to output stream as well??? WHat kind of case is
			// that?
		} catch (Throwable t) {
			throw new RuntimeException("Failed to read action output", t);
		}
	}

	protected File getOutputFile() {
		return new File(invocationInfo.getInstanceFolder() + File.separator + FILE_NAME_OUTPUT);
	}

	private File getLastStatusFile() {
		return getLastStatusFile(invocationInfo.getInstanceFolder());
	}

	protected static File getLastStatusFile(String instanceFolder) {
		return new File(instanceFolder + File.separator + FILE_NAME_LAST_STATUS);
	}

	protected File getFileResumeMethod() {
		return new File(invocationInfo.getInstanceFolder() + File.separator + FILE_NAME_RESUME_METHOD);
	}

	protected File getFileResumeMethodType() {
		return new File(invocationInfo.getInstanceFolder() + File.separator + FILE_NAME_RESUME_METHOD_TYPE);
	}

	protected File getResultFile() {
		return new File(invocationInfo.getInstanceFolder() + File.separator + FILE_NAME_RESULT);
	}

	protected File getInvocationInfoFile() {
		return getInvocationInfoFile(invocationInfo.getInstanceFolder());
	}

	protected static File getInvocationInfoFile(String instanceFolder) {
		return new File(instanceFolder + File.separator + FILE_NAME_INVOCATION_INFO);
	}

	@Override
	public void terminate() {
		if (actionStatus != ActionStatus.InProgressAsync && actionStatus != ActionStatus.InProgressSync) {
			return;
		}

		synchronized (lifecycleSync) {
			terminateInvocationIfAny();
			terminateStatusCheckIfAny();
		}

		setActionStatus(ActionStatus.Terminated);
	}

	private void terminateStatusCheckIfAny() {
		try {
			// Cancel wait interval between status checks if any
			if (statusCheckScheduledFuture != null) {
				statusCheckScheduledFuture.cancel(true);
				statusCheckScheduledFuture = null;
			}

			// Cancel status check watch dog if any
			if (statusCheckWatchdogFuture != null) {
				statusCheckWatchdogFuture.cancel(true);
				statusCheckWatchdogFuture = null;

				// cancel current process if any
				statusCheck.terminate();
				statusCheck = null;
			}
		} catch (Throwable t) {
			throw new RuntimeException("Failed to terminate the action status check: " + toStringForLog(), t);
		}
	}

	private void terminateInvocationIfAny() {
		if (invocationWatchdogFuture == null) {
			return;
		}

		try {
			invocationWatchdogFuture.cancel(true);
			invocationWatchdogFuture = null;

			invocation.terminate();
			invocation = null;
		} catch (Throwable t) {
			throw new RuntimeException("Failed to terminate the action invocation: " + toStringForLog(), t);
		}
	}

	public void setActionStatus(ActionStatus actionStatus) {
		if (actionStatus == this.actionStatus) {
			return;
		}

		if (statusesWhenOutputStreamMustBeClosed.contains(actionStatus)) {
			closeOutputStream();
		}

		try {
			if (invocationInfo != null) {
				FileUtils.write(getLastStatusFile(), actionStatus.name(), ENCODING, false);
			}
			// NOTE: It's not really critical to save this because later on it will be
			// barely used. So it's not completely useless to save it, but it's not
			// critical. The only case when it will be used is when action will be
			// reconciled this status will be used as a previousStatus when reporting status
			// changed.
		} catch (IOException e) {
			log.warn("Failed to dump action " + toStringForLog() + " status " + actionStatus + " to file", e);
		}

		ActionStatus oldStatus = this.actionStatus;
		this.actionStatus = actionStatus;
		actionsExecutionListener.onActionStatusChanged(ActionExecutionSpiImpl.this, oldStatus, actionStatus);
	}

	private static ActionStatus loadLastRecordedActionStatus(String instanceFolder) throws IOException {
		File file = ActionExecutionSpiImpl.getLastStatusFile(instanceFolder);
		String actionStatusStr = FileUtils.readFileToString(file, ENCODING).trim();
		ActionStatus lastRecordedActionStatus = ActionStatus.valueOf(actionStatusStr);
		return lastRecordedActionStatus;
	}

	private void closeOutputStream() {
		if (outputStream == null) {
			return;
		}
		try {
			outputStream.flush();
			outputStream.close();
		} catch (IOException exc) {
			log.warn("There was an error closing output stream for: " + getOutputFile(), exc);
			outputStream = null;
		}
	}

}
