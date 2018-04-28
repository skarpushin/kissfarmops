package org.kissfarmops.agent.node_identity.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.util.List;

import org.junit.Test;
import org.kissfarmops.agent.node_identity.api.PublicIpResolver;
import org.kissfarmops.agent.node_identity.impl.PublicIpResolverLocalIpImpl;
import org.kissfarmops.agent.node_identity.impl.SelfIdentificationFileBasedImpl;
import org.kissfarmops.shared.nodeid.api.NodeIdentity;

public class SelfIdentificationFileBasedImplTest {
	@Test
	public void testResolve() {
		PublicIpResolver publicIpResolver = new PublicIpResolverLocalIpImpl();
		SelfIdentificationFileBasedImpl f = new SelfIdentificationFileBasedImpl("missing");
		f.setPublicIpResolver(publicIpResolver);

		NodeIdentity result = f.resolve();
		assertNotNull(result);
	}

	@Test
	public void testResolveLocalIps() throws Exception {
		List<InetAddress> result = new PublicIpResolverLocalIpImpl().resolveLocalIps();
		assertNotNull(result);
		assertTrue(result.size() > 0);
	}
}
