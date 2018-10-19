package integr.org.kissfarm.crud;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.kissfarm.controller.services.app_instance.api.AppInstanceService;
import org.kissfarm.controller.services.app_instance.dto.AppInstanceRow;
import org.kissfarm.controller.services.nodes.api.Node;
import org.kissfarm.controller.services.nodes.api.NodeService;
import org.kissfarm.shared.config.dto.StatusSchema;
import org.kissfarm.shared.tools.IdTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.ProfileValueSourceConfiguration;
import org.springframework.test.annotation.SystemProfileValueSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@ContextConfiguration("classpath:test-kissfarmcrud-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
@ProfileValueSourceConfiguration(SystemProfileValueSource.class)
@Transactional
public class KissFarmEasyCrudTest {
	@Autowired
	private AppInstanceService appInstanceService;
	@Autowired
	private NodeService nodeService;

	@Test
	public void testAppInstanceRowCanBeCreated() throws Exception {
		Node node = new Node();
		node.setId(IdTools.randomId());
		node.setAgentAuthToken("aat");
		node.setHostName("hn");
		node.setPassword("pwd");
		node.setPublicIp("pip");
		node = nodeService.create(node);

		AppInstanceRow n = new AppInstanceRow();
		n.setId(IdTools.randomId());
		n.setName("app name");
		n.setPrototype("proto name");
		n.setNodeId(node.getId());
		StatusSchema statusSchema = new StatusSchema();
		statusSchema.put("status1", "String");
		statusSchema.put("status2", "Integer");
		n.setStatusSchema(statusSchema);
		appInstanceService.create(n);
		n = appInstanceService.findById(n.getId());
		assertNotNull(n);
		assertNotNull(n.getStatusSchema());
		assertEquals(2, n.getStatusSchema().size());
	}
}
