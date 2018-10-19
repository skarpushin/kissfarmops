package integr.org.kissfarm.crud;

import org.kissfarm.controller.services.app_instance.api.AppInstanceService;
import org.kissfarm.controller.services.nodes.api.NodeService;
import org.kissfarm.shared.config.dto.StatusSchemaFromStringConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.summerb.approaches.jdbccrud.scaffold.api.EasyCrudScaffold;

@Configuration
public class ScaffoldedConfigForTests {
	@Autowired
	private EasyCrudScaffold easyCrudScaffold;

	@Bean
	public NodeService nodeService() {
		// NOTE: This is just a dummy impl, super simple just ot be able to CRUD nodes
		return easyCrudScaffold.fromService(NodeService.class, NodeService.TERM, "nodes");
	}

	@Bean
	public AppInstanceService appInstanceService() {
		return easyCrudScaffold.fromService(AppInstanceService.class, AppInstanceService.TERM, "app_instance");
	}
}
