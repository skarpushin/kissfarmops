package org.kissfarm.agent.node_identity.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.util.List;

import org.junit.Test;
import org.kissfarm.agent.node_identity.api.PublicIpResolver;
import org.kissfarm.shared.api.NodeIdentity;

public class SelfIdentificationFileBasedImplTest {
	@Test
	public void testResolve() {
		PublicIpResolver publicIpResolver = new PublicIpResolverLocalIpImpl();
		SelfIdentificationFileBasedImpl f = new SelfIdentificationFileBasedImpl();
		f.setNodeIdentityFilename("missing");
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