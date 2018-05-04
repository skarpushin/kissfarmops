package org.kissfarm.controller.services.nodes.impl;

import org.kissfarm.controller.services.nodes.api.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.summerb.approaches.jdbccrud.api.exceptions.EntityNotFoundException;
import org.summerb.approaches.jdbccrud.impl.EasyCrudExceptionStrategyDefaultImpl;
import org.summerb.approaches.security.api.exceptions.NotAuthorizedException;
import org.summerb.approaches.validation.FieldValidationException;
import org.summerb.approaches.validation.ValidationError;
import org.summerb.microservices.users.api.dto.User;
import org.summerb.utils.exceptions.ExceptionUtils;

public class NodeExceptionStrategy extends EasyCrudExceptionStrategyDefaultImpl<Node> {
	private Logger log = LoggerFactory.getLogger(getClass());

	public NodeExceptionStrategy(String entityTypeMessageCode) {
		super(entityTypeMessageCode);
	}

	@Override
	public RuntimeException handleExceptionAtCreate(Throwable t)
			throws FieldValidationException, NotAuthorizedException {

		FieldValidationException fve = ExceptionUtils.findExceptionOfType(t, FieldValidationException.class);
		if (fve != null) {
			convertUserFveTonodeFve(fve);
		}

		return super.handleExceptionAtCreate(t);
	}

	@Override
	public RuntimeException handleExceptionAtUpdate(Throwable t)
			throws FieldValidationException, NotAuthorizedException, EntityNotFoundException {

		FieldValidationException fve = ExceptionUtils.findExceptionOfType(t, FieldValidationException.class);
		if (fve != null) {
			convertUserFveTonodeFve(fve);
		}

		return super.handleExceptionAtUpdate(t);
	}

	private void convertUserFveTonodeFve(FieldValidationException fve) {
		fve.getErrors().forEach(x -> mapUserFieldToNodeField(x));
	}

	private void mapUserFieldToNodeField(ValidationError x) {
		if (User.FN_EMAIL.equals(x.getFieldToken())) {
			x.setFieldToken(Node.FN_ID);
		} else if (User.FN_DISPLAY_NAME.equals(x.getFieldToken())) {
			x.setFieldToken(Node.FN_HOST_NAME);
		} else if ("uuid".equals(x.getFieldToken())) {
			x.setFieldToken(Node.FN_ID);
		}
	}
}
