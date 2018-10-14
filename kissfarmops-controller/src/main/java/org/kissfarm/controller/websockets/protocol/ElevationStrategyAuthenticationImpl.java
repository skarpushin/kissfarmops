package org.kissfarm.controller.websockets.protocol;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.summerb.approaches.springmvc.security.apis.ElevationStrategy;

public class ElevationStrategyAuthenticationImpl implements ElevationStrategy {
	private Authentication authentication;

	public ElevationStrategyAuthenticationImpl(Authentication authentication) {
		this.authentication = authentication;
	}

	@Override
	public boolean isElevationRequired() {
		// TBD-IMPROVE: Check if current user matches requested user then elevation not
		// requried
		return true;
	}

	@Override
	public Object elevate() {
		SecurityContext ret = SecurityContextHolder.getContext();
		SecurityContext context = new SecurityContextImpl();
		context.setAuthentication(authentication);
		SecurityContextHolder.setContext(context);
		return ret;
	}

	@Override
	public void deElevate(Object previousContext) {
		if (previousContext == null) {
			SecurityContextHolder.clearContext();
		} else {
			SecurityContextHolder.setContext((SecurityContext) previousContext);
		}
	}
}
