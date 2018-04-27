package org.kissfarmops.agent.process_execution.impl;

import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;
import org.kissfarmops.agent.process_execution.api.ProcessExecutionCallback;
import org.kissfarmops.agent.process_execution.impl.ProcessExecutionImpl;

public class ProcessExecutionImplTest {

	@Test
	public void testReplaceEnvVarsInCommand_expectAllReplaced() throws InterruptedException, TimeoutException {
		String[] command = { "a $a %a% $b %b% $c$ %d" };

		Map<String, String> values = new HashMap<>();
		values.put("a", "1");
		values.put("b", "2");
		values.put("c", "3");
		values.put("d", "4");

		String[] result = ProcessExecutionImpl.replaceEnvVarsInCommand(command, values);

		if (ProcessExecutionImpl.isWindows()) {
			assertEquals("a $a 1 $b 2 $c$ %d", result[0]);
		} else {
			assertEquals("a 1 %a% 2 %b% 3$ %d", result[0]);
		}
	}

	@Test
	public void testExpectTextWillBeReturnedAndEnvVariableUsed() throws InterruptedException, TimeoutException {
		StringBuilder sb = new StringBuilder();
		AtomicInteger statusCode = new AtomicInteger(Integer.MIN_VALUE);

		ProcessExecutionCallback callback = buildCallback(sb, statusCode);
		ProcessExecutionImpl f = new ProcessExecutionImpl(null, Collections.singletonMap("ARG21", "world"), callback,
				"sh", "-c", "echo Hello $ARG21");
		f.blockUntilFinished(10000);

		assertEquals(0, statusCode.get());
		assertEquals("Hello world", sb.toString().trim());
	}

	@Test
	public void testExpectTextWillBeReturnedAndEnvVariableWillbeSubstituted()
			throws InterruptedException, TimeoutException {
		StringBuilder sb = new StringBuilder();
		AtomicInteger statusCode = new AtomicInteger(Integer.MIN_VALUE);

		ProcessExecutionCallback callback = buildCallback(sb, statusCode);
		ProcessExecutionImpl f = new ProcessExecutionImpl(null, Collections.singletonMap("ARG21", "world"), callback,
				"echo", "Hello $ARG21");
		f.blockUntilFinished(10000);

		assertEquals(0, statusCode.get());
		assertEquals("Hello world", sb.toString().trim());
	}

	private ProcessExecutionCallback buildCallback(StringBuilder sb, AtomicInteger statusCode) {
		return new ProcessExecutionCallback() {
			@Override
			public void onProcessFinished(int exitCode) {
				statusCode.set(exitCode);
			}

			@Override
			public void onOutput(byte[] output) {
				try {
					String out = new String(output, "UTF-8");
					sb.append(out);
				} catch (UnsupportedEncodingException e) {
					throw new RuntimeException("Failed to handle bytes", e);
				}
			}

			@Override
			public void onError(byte[] error) {
				fail("error not expected");
			}
		};
	}
}
