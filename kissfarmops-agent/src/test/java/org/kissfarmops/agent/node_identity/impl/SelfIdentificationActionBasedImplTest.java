package org.kissfarmops.agent.node_identity.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.junit.Test;
import org.kissfarmops.agent.action_executor.api.ActionExecutionFactory;
import org.kissfarmops.agent.action_executor.impl_folder.ActionExecutionFactoryImpl;
import org.kissfarmops.agent.node_identity.impl.PublicIpResolverLocalIpImpl;
import org.kissfarmops.agent.node_identity.impl.SelfIdentificationActionBasedImpl;
import org.kissfarmops.agent.process_execution.api.ProcessExecutorFactory;
import org.kissfarmops.agent.process_execution.impl.ProcessExecutorFactoryImpl;
import org.kissfarmops.agent.serializer.api.DtoSerializer;
import org.kissfarmops.agent.serializer.impl.DtoSerializerGsonImpl;
import org.kissfarmops.shared.nodeid.api.NodeIdentity;

public class SelfIdentificationActionBasedImplTest {
	private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(16);
	private DtoSerializer dtoSerializer = new DtoSerializerGsonImpl();
	private ProcessExecutorFactory processExecutorFactory = new ProcessExecutorFactoryImpl();
	private ActionExecutionFactory actionExecutionFactory = new ActionExecutionFactoryImpl(dtoSerializer,
			executorService, processExecutorFactory);

	@Test
	public void testResolve() {
		SelfIdentificationActionBasedImpl f = new SelfIdentificationActionBasedImpl(actionExecutionFactory,
				new String[] { "sh", "-c", "echo {\\\"id\\\":\\\"test\\\"}" }, ".");
		f.setPublicIpResolver(new PublicIpResolverLocalIpImpl());

		NodeIdentity result = f.resolve();

		assertNotNull(result);
		assertEquals("test", result.getId());
	}
}
