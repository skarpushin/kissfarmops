package org.kissfarm.controller.services.nodes.impl;

import java.util.Locale;

import org.kissfarm.controller.services.agent_auth_token.api.AgentAuthToken;
import org.kissfarm.controller.services.agent_auth_token.api.AgentAuthTokenService;
import org.kissfarm.controller.services.nodes.api.Node;
import org.kissfarm.shared.api.InvalidAgentAuthTokenValidationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.summerb.approaches.jdbccrud.impl.EasyCrudValidationStrategyAbstract;
import org.summerb.approaches.springmvc.security.apis.ElevationRunner;
import org.summerb.approaches.validation.ValidationContext;
import org.summerb.utils.exceptions.translator.ExceptionTranslator;

import com.google.common.base.Preconditions;

public class NodeValidationStrategyImpl extends EasyCrudValidationStrategyAbstract<Node> {
	private Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	private AgentAuthTokenService agentAuthTokenService;
	@Autowired
	private ExceptionTranslator exceptionTranslator;
	@Autowired
	private ElevationRunner backgroundProcessRightsElevation;

	@Override
	protected void doValidateForCreate(Node dto, ValidationContext ctx) {
		ctx.validateNotEmpty(dto.getId(), Node.FN_ID);

		if (ctx.validateNotEmpty(dto.getAgentAuthToken(), Node.FN_AGENT_AUTH_TOKEN)) {
			try {
				AgentAuthToken agentAuthToken = backgroundProcessRightsElevation
						.callElevated(() -> agentAuthTokenService.findById(dto.getAgentAuthToken()));
				Preconditions.checkArgument(agentAuthToken != null, "Incorrect agentAuthToken");
				Preconditions.checkArgument(agentAuthToken.isEnabled(), "Can't use disabled agentAuthToken");
			} catch (Throwable e) {
				log.warn("Invalid agentAuthToken: " + dto.getAgentAuthToken(), e);
				ctx.add(new InvalidAgentAuthTokenValidationError(Node.FN_AGENT_AUTH_TOKEN,
						exceptionTranslator.buildUserMessage(e, Locale.ENGLISH)));
			}
		}

		if (ctx.validateNotEmpty(dto.getHostName(), Node.FN_HOST_NAME)) {
			ctx.validateDataLengthLessOrEqual(dto.getHostName(), Node.FN_HOST_NAME_SIZE, Node.FN_HOST_NAME);
		}

		if (ctx.validateNotEmpty(dto.getPassword(), Node.FN_PASSWORD)) {
			ctx.validateDataLengthLessOrEqual(dto.getPassword(), Node.FN_PASSWORD_SIZE, Node.FN_PASSWORD);
		}

		if (ctx.validateNotEmpty(dto.getPublicIp(), Node.FN_PUBLIC_IP)) {
			ctx.validateDataLengthLessOrEqual(dto.getPublicIp(), Node.FN_PUBLIC_IP_SIZE, Node.FN_PUBLIC_IP);
		}
	}

}
