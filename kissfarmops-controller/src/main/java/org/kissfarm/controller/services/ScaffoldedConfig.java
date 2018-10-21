package org.kissfarm.controller.services;

import org.kissfarm.controller.security.impl.AuthenticatedUsersTableAuthStrategy;
import org.kissfarm.controller.security.impl.AuthenticatedUsersTableAuthStrategyImpl;
import org.kissfarm.controller.services.agent_auth_token.api.AgentAuthTokenService;
import org.kissfarm.controller.services.agent_auth_token.impl.AgentAuthTokenAuthStrategy;
import org.kissfarm.controller.services.agent_auth_token.impl.AgentAuthTokenAuthStrategyImpl;
import org.kissfarm.controller.services.agent_auth_token.impl.AgentAuthTokenValidationStrategyImpl;
import org.kissfarm.controller.services.app_instance.api.ActionStatusService;
import org.kissfarm.controller.services.app_instance.api.AppInstanceService;
import org.kissfarm.controller.services.nodes.api.Node;
import org.kissfarm.controller.services.nodes.api.NodeAuthStrategy;
import org.kissfarm.controller.services.nodes.api.NodeService;
import org.kissfarm.controller.services.nodes.api.NodeStatusOnlinePatchWireTap;
import org.kissfarm.controller.services.nodes.api.NodeStatusService;
import org.kissfarm.controller.services.nodes.impl.NodeAuthStrategyImpl;
import org.kissfarm.controller.services.nodes.impl.NodeExceptionStrategy;
import org.kissfarm.controller.services.nodes.impl.NodeToUserReplicationWireTap;
import org.kissfarm.controller.services.nodes.impl.NodeValidationStrategyImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.summerb.approaches.jdbccrud.api.EasyCrudExceptionStrategy;
import org.summerb.approaches.jdbccrud.api.EasyCrudValidationStrategy;
import org.summerb.approaches.jdbccrud.api.EasyCrudWireTap;
import org.summerb.approaches.jdbccrud.impl.wireTaps.EasyCrudWireTapEventBusImpl;
import org.summerb.approaches.jdbccrud.scaffold.api.EasyCrudScaffold;

import com.google.common.eventbus.EventBus;

@Configuration
public class ScaffoldedConfig {
	@Autowired
	private EasyCrudScaffold easyCrudScaffold;
	@Autowired
	private EventBus entityChangesEventBus;

	@Bean
	public AgentAuthTokenAuthStrategy agentAuthTokenAuthStrategy() {
		return new AgentAuthTokenAuthStrategyImpl();
	}

	@Bean
	public EasyCrudWireTap<?, ?> wireTapEventBusForwarder() {
		return new EasyCrudWireTapEventBusImpl<>(entityChangesEventBus);
	}

	@Bean
	public AgentAuthTokenService agentAuthTokenService() {
		return easyCrudScaffold.fromService(AgentAuthTokenService.class, AgentAuthTokenService.TERM,
				"agent_auth_tokens", new AgentAuthTokenValidationStrategyImpl(), agentAuthTokenAuthStrategy(),
				wireTapEventBusForwarder());
	}

	@Bean
	public NodeAuthStrategy nodeAuthStrategy() {
		return new NodeAuthStrategyImpl();
	}

	@Bean
	public EasyCrudWireTap<String, Node> nodeToUserReplicationWireTap() {
		return new NodeToUserReplicationWireTap();
	}

	@Bean
	public EasyCrudExceptionStrategy<Node> nodeExceptionStrategy() {
		return new NodeExceptionStrategy(NodeService.TERM);
	}

	@Bean
	public EasyCrudValidationStrategy<Node> nodeValidationStrategyImpl() {
		return new NodeValidationStrategyImpl();
	}

	@Bean
	public NodeService nodeService() {
		return easyCrudScaffold.fromService(NodeService.class, NodeService.TERM, "nodes", nodeExceptionStrategy(),
				nodeValidationStrategyImpl(), nodeAuthStrategy(), nodeToUserReplicationWireTap(),
				wireTapEventBusForwarder());
	}

	@Bean
	public NodeStatusService nodeNodeStatusService() {
		return easyCrudScaffold.fromService(NodeStatusService.class, NodeStatusService.TERM, "node_status",
				new NodeStatusOnlinePatchWireTap(), wireTapEventBusForwarder());
	}

	@Bean
	public AuthenticatedUsersTableAuthStrategy authenticatedUsersTableAuthStrategy() {
		return new AuthenticatedUsersTableAuthStrategyImpl();
	}

	@Bean
	public AppInstanceService appInstanceService() {
		return easyCrudScaffold.fromService(AppInstanceService.class, AppInstanceService.TERM, "app_instance",
				authenticatedUsersTableAuthStrategy(), wireTapEventBusForwarder());
	}

	@Bean
	public ActionStatusService actionStatusService() {
		return easyCrudScaffold.fromService(ActionStatusService.class, ActionStatusService.TERM, "action_status",
				authenticatedUsersTableAuthStrategy(), wireTapEventBusForwarder());
	}

}
