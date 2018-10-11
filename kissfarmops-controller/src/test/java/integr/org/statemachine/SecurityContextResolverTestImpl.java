package integr.org.statemachine;

import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.util.StringUtils;
import org.summerb.approaches.security.api.SecurityContextResolver;
import org.summerb.approaches.springmvc.security.SecurityConstants;
import org.summerb.approaches.springmvc.security.dto.UserDetailsImpl;
import org.summerb.microservices.users.api.dto.User;

import com.google.common.base.Preconditions;

public class SecurityContextResolverTestImpl implements SecurityContextResolver<User> {
	public static final String USER_UUID = UUID.randomUUID().toString();
	public static final String USER_EMAIL = "u" + Long.toString(System.currentTimeMillis(), Character.MAX_RADIX)
			+ "@email.com";

	public SecurityContextResolverTestImpl() {
		SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_GLOBAL);

		SecurityContextImpl context = new SecurityContextImpl();
		User user = new User();
		user.setEmail(USER_EMAIL);
		user.setUuid(USER_UUID);
		UserDetailsImpl userDetails = new UserDetailsImpl(user, null, Arrays.asList(SecurityConstants.ROLE_USER), null);
		TestingAuthenticationToken authentication = new TestingAuthenticationToken(userDetails, null,
				SecurityConstants.ROLE_USER);
		context.setAuthentication(authentication);
		SecurityContextHolder.setContext(context);
	}

	@Override
	public SecurityContext resolveSecurityContext() {
		return SecurityContextHolder.getContext();
	}

	@Override
	public User getUser() {
		return ((UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();
	}

	@Override
	public Collection<? extends GrantedAuthority> getCurrentUserGlobalPermissions() {
		SecurityContext context = resolveSecurityContext();
		Authentication auth = context.getAuthentication();
		return auth.getAuthorities();
	}

	@Override
	public boolean hasRole(String role) {
		Preconditions.checkArgument(StringUtils.hasText(role));

		Collection<? extends GrantedAuthority> permissions = getCurrentUserGlobalPermissions();
		for (GrantedAuthority ga : permissions) {
			if (role.equals(ga.getAuthority())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String getUserUuid() {
		return getUser().getUuid();
	}

	@Override
	public boolean hasAnyRole(String... roles) {
		for (String role : roles) {
			if (hasRole(role)) {
				return true;
			}
		}
		return false;
	}

}
