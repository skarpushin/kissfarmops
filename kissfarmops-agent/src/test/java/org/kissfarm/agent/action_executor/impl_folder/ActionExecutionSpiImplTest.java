package org.kissfarm.agent.action_executor.impl_folder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kissfarm.agent.action_executor.api.ActionExecutionSpi;
import org.kissfarm.agent.action_executor.api.ActionInvocationInfo;
import org.kissfarm.agent.action_executor.api.ActionsExecutionListener;
import org.kissfarm.agent.process_execution.api.ProcessExecutorFactory;
import org.kissfarm.agent.process_execution.impl.ProcessExecutorFactoryImpl;
import org.kissfarm.agent.serializer.api.DtoSerializer;
import org.kissfarm.agent.serializer.impl.DtoSerializerGsonImpl;
import org.kissfarm.shared.config.dto.ActionCommands;
import org.kissfarm.shared.config.dto.ActionStatus;
import org.kissfarm.shared.tools.IdTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;

/**
 * IMPORTANT: This set of test cases is really platform-dependent and can pass
 * only on linux platform
 * 
 * @author Sergey Karpushin
 *
 */
public class ActionExecutionSpiImplTest {
	private static Logger log = LoggerFactory.getLogger(ActionExecutionSpiImplTest.class);

	private File instanceFolder;

	private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(16);
	private DtoSerializer dtoSerializer = new DtoSerializerGsonImpl();
	private ProcessExecutorFactory processExecutorFactory = new ProcessExecutorFactoryImpl();

	@Before
	public void beforeEachTest() {
		instanceFolder = Files.createTempDir();
	}

	@After
	public void afterEachTest() {
		try {
			if (instanceFolder != null) {
				FileUtils.forceDeleteOnExit(instanceFolder);
			}
		} catch (IOException e) {
			// don't care too much
		}
	}

	@Test
	public void smokeTestForSyncOperation() throws InterruptedException {
		ActionCommands actionCommands = new ActionCommands();
		actionCommands.setInvoke(new String[] { "sh", "-c", "echo Echoing $VAR1" });
		Map<String, String> envVars = Collections.singletonMap("VAR1", "456");
		String scriptsFolder = System.getProperty("user.home");
		ActionInvocationInfo invocationInfo = new ActionInvocationInfo("actionName1", IdTools.randomId(), scriptsFolder,
				actionCommands, instanceFolder.getAbsolutePath(), envVars);

		ActionsExecutionListener actionsExecutionListener = new ActionsExecutionListenerLoggingImpl();
		ActionExecutionSpi f = ActionExecutionSpiImpl.startNewInvocation(invocationInfo, dtoSerializer, executorService,
				processExecutorFactory, actionsExecutionListener);

		long now = System.currentTimeMillis();
		while (System.currentTimeMillis() - now < 2000 && f.getStatus() != ActionStatus.Success) {
			assertNotEquals(ActionStatus.InProgressAsync, f.getStatus());
			assertNotEquals(ActionStatus.Failed, f.getStatus());
			assertNotEquals(ActionStatus.Terminated, f.getStatus());
			assertNotEquals(ActionStatus.Timedout, f.getStatus());
			assertNotEquals(ActionStatus.Exception, f.getStatus());
			Thread.sleep(50);
		}

		assertEquals(ActionStatus.Success, f.getStatus());
		assertEquals("Echoing 456", f.getOutput().trim());
	}

	@Test
	public void postponedActionCanBeReconciledOk() throws Exception {
		ActionCommands actionCommands = new ActionCommands();
		actionCommands.setInvoke(new String[] { "sh", "-c", "echo Echoing $VAR1" });
		Map<String, String> envVars = Collections.singletonMap("VAR1", "456");
		String scriptsFolder = System.getProperty("user.home");
		ActionInvocationInfo invocationInfo = new ActionInvocationInfo("actionName1", IdTools.randomId(), scriptsFolder,
				actionCommands, instanceFolder.getAbsolutePath(), envVars);

		// === Postpone
		ActionsExecutionListener actionsExecutionListener = new ActionsExecutionListenerLoggingImpl();
		ActionExecutionSpi postponed = ActionExecutionSpiImpl.postponeAction(invocationInfo, dtoSerializer,
				actionsExecutionListener);
		assertEquals(ActionStatus.Postponed, postponed.getStatus());

		// === Reconcile
		ActionExecutionSpi f = ActionExecutionSpiImpl.reconcileExistingAction(instanceFolder.getAbsolutePath(),
				dtoSerializer, executorService, processExecutorFactory, actionsExecutionListener);
		long now = System.currentTimeMillis();
		while (System.currentTimeMillis() - now < 2000 && f.getStatus() != ActionStatus.Success) {
			assertNotEquals(ActionStatus.InProgressAsync, f.getStatus());
			assertNotEquals(ActionStatus.Failed, f.getStatus());
			assertNotEquals(ActionStatus.Terminated, f.getStatus());
			assertNotEquals(ActionStatus.Timedout, f.getStatus());
			assertNotEquals(ActionStatus.Exception, f.getStatus());
			Thread.sleep(50);
		}

		assertEquals(ActionStatus.Success, f.getStatus());
		assertEquals("Echoing 456", f.getOutput().trim());
	}

	@Test
	public void smokeTestForAsyncOperation() throws InterruptedException {
		ActionCommands actionCommands = new ActionCommands();
		actionCommands.setInvoke(new String[] { "sh", "-c", "exit 3" });
		actionCommands.setStatusCheckIntervalMs(200);
		actionCommands.setCheckStatus(new String[] { "sh", "-c",
				"echo $(($(date +%s%N)/1000000)); exit $(( $(date +%s%N)/1000000 < $VAR1 ? 3 : 0 ))" });
		Map<String, String> envVars = Collections.singletonMap("VAR1", "" + (System.currentTimeMillis() + 500));
		String scriptsFolder = System.getProperty("user.home");
		ActionInvocationInfo invocationInfo = new ActionInvocationInfo("actionName1", IdTools.randomId(), scriptsFolder,
				actionCommands, instanceFolder.getAbsolutePath(), envVars);

		ActionsExecutionListener actionsExecutionListener = new ActionsExecutionListenerLoggingImpl();
		ActionExecutionSpi f = ActionExecutionSpiImpl.startNewInvocation(invocationInfo, dtoSerializer, executorService,
				processExecutorFactory, actionsExecutionListener);

		long now = System.currentTimeMillis();
		while (System.currentTimeMillis() - now < 2000 && f.getStatus() != ActionStatus.Success) {
			assertNotEquals(ActionStatus.Failed, f.getStatus());
			assertNotEquals(ActionStatus.Terminated, f.getStatus());
			assertNotEquals(ActionStatus.Timedout, f.getStatus());
			assertNotEquals(ActionStatus.Exception, f.getStatus());
			Thread.sleep(50);
		}

		assertEquals(ActionStatus.Success, f.getStatus());
		assertTrue(f.getOutput().length() > 0);
	}

	@Test
	public void smokeTestForPostponedAsyncOperation() throws InterruptedException {
		ActionCommands actionCommands = new ActionCommands();
		actionCommands.setInvoke(new String[] { "sh", "-c", "sleep 1s; exit 3" });
		actionCommands.setStatusCheckIntervalMs(400);
		actionCommands.setCheckStatus(new String[] { "sh", "-c",
				"echo $(($(date +%s%N)/1000000)); exit $(( $(date +%s%N)/1000000 < $VAR1 ? 3 : 0 ))" });
		Map<String, String> envVars = Collections.singletonMap("VAR1", "" + (System.currentTimeMillis() + 500));
		String scriptsFolder = System.getProperty("user.home");
		ActionInvocationInfo invocationInfo = new ActionInvocationInfo("actionName1", IdTools.randomId(), scriptsFolder,
				actionCommands, instanceFolder.getAbsolutePath(), envVars);

		ActionsExecutionListener actionsExecutionListener = new ActionsExecutionListenerLoggingImpl();

		// === Start
		ActionExecutionSpi suspended = ActionExecutionSpiImpl.startNewInvocation(invocationInfo, dtoSerializer,
				executorService, processExecutorFactory, actionsExecutionListener);

		// === Suspend
		suspended.suspend();
		long now = System.currentTimeMillis();
		while (System.currentTimeMillis() - now < 1500 && suspended.getStatus() != ActionStatus.Suspended) {
			Thread.sleep(50);
		}
		assertEquals(ActionStatus.Suspended, suspended.getStatus());

		// === Reconcile
		ActionExecutionSpi f = ActionExecutionSpiImpl.reconcileExistingAction(instanceFolder.getAbsolutePath(),
				dtoSerializer, executorService, processExecutorFactory, actionsExecutionListener);

		now = System.currentTimeMillis();
		while (System.currentTimeMillis() - now < 2000 && f.getStatus() != ActionStatus.Success) {
			assertNotEquals(ActionStatus.Failed, f.getStatus());
			assertNotEquals(ActionStatus.Terminated, f.getStatus());
			assertNotEquals(ActionStatus.Timedout, f.getStatus());
			assertNotEquals(ActionStatus.Exception, f.getStatus());
			Thread.sleep(50);
		}

		assertEquals(ActionStatus.Success, f.getStatus());
		assertTrue(f.getOutput().length() > 0);
	}

	@Test
	public void smokeTestRealWorldCaseAsync() throws InterruptedException, URISyntaxException {
		ActionCommands actionCommands = new ActionCommands();
		actionCommands.setStatusCheckIntervalMs(200);
		actionCommands.setInvoke(new String[] { "sh", "invoke.sh" });
		actionCommands.setCheckStatus(new String[] { "sh", "check-status.sh" });
		Map<String, String> envVars = Collections.singletonMap("VAR1", "env var from code");
		String scriptsFolder = new File(getClass().getClassLoader().getResource("action2").toURI()).getAbsolutePath();
		ActionInvocationInfo invocationInfo = new ActionInvocationInfo("test2", IdTools.randomId(), scriptsFolder,
				actionCommands, instanceFolder.getAbsolutePath(), envVars);

		ActionsExecutionListener actionsExecutionListener = new ActionsExecutionListenerLoggingImpl();
		ActionExecutionSpi f = ActionExecutionSpiImpl.startNewInvocation(invocationInfo, dtoSerializer, executorService,
				processExecutorFactory, actionsExecutionListener);

		long now = System.currentTimeMillis();
		while (System.currentTimeMillis() - now < 2000 && f.getStatus() != ActionStatus.Success) {
			if (ActionStatus.Failed == f.getStatus()) {
				log.warn("Operation failed. Output: \r\n" + f.getOutput());
				fail("Operation failed");
			}

			assertNotEquals(ActionStatus.Terminated, f.getStatus());
			assertNotEquals(ActionStatus.Timedout, f.getStatus());
			assertNotEquals(ActionStatus.Exception, f.getStatus());
			Thread.sleep(50);
		}

		assertEquals(ActionStatus.Success, f.getStatus());
		assertEquals("{\"result\": \"env var from code\"}", f.getResult().trim());
	}

	@Test
	public void gracefulStartFailure() throws InterruptedException {
		ActionCommands actionCommands = new ActionCommands();
		actionCommands.setInvoke(new String[] { "missing_process" });
		String scriptsFolder = System.getProperty("user.home");
		ActionInvocationInfo invocationInfo = new ActionInvocationInfo("invocationFailed", IdTools.randomId(),
				scriptsFolder, actionCommands, instanceFolder.getAbsolutePath(), new HashMap<>());

		ActionsExecutionListener actionsExecutionListener = new ActionsExecutionListenerLoggingImpl();
		ActionExecutionSpi f = ActionExecutionSpiImpl.startNewInvocation(invocationInfo, dtoSerializer, executorService,
				processExecutorFactory, actionsExecutionListener);

		long now = System.currentTimeMillis();
		while (System.currentTimeMillis() - now < 2000 && f.getStatus() != ActionStatus.Exception) {
			assertNotEquals(ActionStatus.InProgressAsync, f.getStatus());
			assertNotEquals(ActionStatus.Success, f.getStatus());
			assertNotEquals(ActionStatus.Terminated, f.getStatus());
			assertNotEquals(ActionStatus.Timedout, f.getStatus());
			assertNotEquals(ActionStatus.Failed, f.getStatus());
			Thread.sleep(50);
		}

		assertEquals(ActionStatus.Exception, f.getStatus());
		assertTrue(f.getOutput().length() > 0);
		assertTrue(f.getOutput().contains("Exception"));
		assertTrue(f.getOutput().contains("Caused by"));
		log.trace("Dunmping output from the test: \r\n" + f.getOutput());
	}

	@Test
	public void gracefulStatusCheckFailure() throws InterruptedException {
		ActionCommands actionCommands = new ActionCommands();
		actionCommands.setInvoke(new String[] { "sh", "-c", "exit 3" });
		actionCommands.setCheckStatus(new String[] { "missing_process" });
		actionCommands.setStatusCheckIntervalMs(200);
		String scriptsFolder = System.getProperty("user.home");
		ActionInvocationInfo invocationInfo = new ActionInvocationInfo("statusCheckFailed", IdTools.randomId(),
				scriptsFolder, actionCommands, instanceFolder.getAbsolutePath(), new HashMap<>());

		ActionsExecutionListener actionsExecutionListener = new ActionsExecutionListenerLoggingImpl();
		ActionExecutionSpi f = ActionExecutionSpiImpl.startNewInvocation(invocationInfo, dtoSerializer, executorService,
				processExecutorFactory, actionsExecutionListener);

		long now = System.currentTimeMillis();
		while (System.currentTimeMillis() - now < 2000 && f.getStatus() != ActionStatus.Exception) {
			assertNotEquals(ActionStatus.Success, f.getStatus());
			assertNotEquals(ActionStatus.Terminated, f.getStatus());
			assertNotEquals(ActionStatus.Timedout, f.getStatus());
			assertNotEquals(ActionStatus.Failed, f.getStatus());
			Thread.sleep(50);
		}

		assertEquals(ActionStatus.Exception, f.getStatus());
		assertTrue(f.getOutput().length() > 0);
		assertTrue(f.getOutput().contains("Exception"));
		assertTrue(f.getOutput().contains("Caused by"));
		log.trace("Dunmping output from the test: \r\n" + f.getOutput());
	}

	@Test
	public void gracefulHandleInvocationTimeout() throws InterruptedException {
		ActionCommands actionCommands = new ActionCommands();
		actionCommands.setInvocationTimeoutMs(300);
		actionCommands.setInvoke(new String[] { "sh", "-c", "sleep 0.5" });
		String scriptsFolder = System.getProperty("user.home");
		ActionInvocationInfo invocationInfo = new ActionInvocationInfo("timedOutInvocation", IdTools.randomId(),
				scriptsFolder, actionCommands, instanceFolder.getAbsolutePath(), new HashMap<>());

		ActionsExecutionListener actionsExecutionListener = new ActionsExecutionListenerLoggingImpl();
		ActionExecutionSpi f = ActionExecutionSpiImpl.startNewInvocation(invocationInfo, dtoSerializer, executorService,
				processExecutorFactory, actionsExecutionListener);

		long now = System.currentTimeMillis();
		while (System.currentTimeMillis() - now < 2000 && f.getStatus() != ActionStatus.Timedout) {
			assertNotEquals(ActionStatus.Success, f.getStatus());
			assertNotEquals(ActionStatus.Terminated, f.getStatus());
			assertNotEquals(ActionStatus.Exception, f.getStatus());
			assertNotEquals(ActionStatus.Failed, f.getStatus());
			Thread.sleep(50);
		}

		assertEquals(ActionStatus.Timedout, f.getStatus());
	}

	@Test
	public void gracefulHandleStatusCheckTimeout() throws InterruptedException {
		ActionCommands actionCommands = new ActionCommands();
		actionCommands.setInvoke(new String[] { "sh", "-c", "exit 3" });
		actionCommands.setCheckStatus(new String[] { "sh", "-c", "sleep 0.5" });
		actionCommands.setStatusCheckIntervalMs(200);
		actionCommands.setStatusCheckTimeoutMs(200);
		String scriptsFolder = System.getProperty("user.home");
		ActionInvocationInfo invocationInfo = new ActionInvocationInfo("timedOutStatus", IdTools.randomId(),
				scriptsFolder, actionCommands, instanceFolder.getAbsolutePath(), new HashMap<>());

		ActionsExecutionListener actionsExecutionListener = new ActionsExecutionListenerLoggingImpl();
		ActionExecutionSpi f = ActionExecutionSpiImpl.startNewInvocation(invocationInfo, dtoSerializer, executorService,
				processExecutorFactory, actionsExecutionListener);

		long now = System.currentTimeMillis();
		while (System.currentTimeMillis() - now < 2000 && f.getStatus() != ActionStatus.Timedout) {
			assertNotEquals(ActionStatus.Success, f.getStatus());
			assertNotEquals(ActionStatus.Terminated, f.getStatus());
			assertNotEquals(ActionStatus.Exception, f.getStatus());
			assertNotEquals(ActionStatus.Failed, f.getStatus());
			Thread.sleep(50);
		}

		assertEquals(ActionStatus.Timedout, f.getStatus());
	}

	// TODO: Test suspension of action - invocation timeout watch dog

	// TODO: Test suspension of action - status check delay

	// TODO: Test suspension of action - status check exit code

	// TODO: Test suspension of action - status check timeout watch dog

	// TODO: Test reconcile of async action

	// TODO: Test reconcile of horribly failed action (like we started a process and
	// everything died)
}
