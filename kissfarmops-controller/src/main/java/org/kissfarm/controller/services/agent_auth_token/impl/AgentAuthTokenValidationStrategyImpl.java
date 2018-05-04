package org.kissfarm.controller.services.agent_auth_token.impl;

import org.kissfarm.controller.services.agent_auth_token.api.AgentAuthToken;
import org.springframework.util.StringUtils;
import org.summerb.approaches.jdbccrud.impl.EasyCrudValidationStrategyAbstract;
import org.summerb.approaches.validation.ValidationContext;

public class AgentAuthTokenValidationStrategyImpl extends EasyCrudValidationStrategyAbstract<AgentAuthToken> {

	@Override
	protected void doValidateForCreate(AgentAuthToken dto, ValidationContext ctx) {
		if (StringUtils.hasText(dto.getComment())) {
			ctx.validateDataLengthLessOrEqual(dto.getComment(), AgentAuthToken.FN_COMMENT_SIZE,
					AgentAuthToken.FN_COMMENT);
		}
	}

}
