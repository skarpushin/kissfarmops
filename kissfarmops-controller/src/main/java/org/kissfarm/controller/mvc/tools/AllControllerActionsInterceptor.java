package org.kissfarm.controller.mvc.tools;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.LocaleContextResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;
import org.summerb.approaches.security.api.CurrentUserNotFoundException;
import org.summerb.approaches.security.api.SecurityContextResolver;
import org.summerb.approaches.springmvc.controllers.ControllerBase;
import org.summerb.microservices.users.api.dto.User;

import com.google.gson.Gson;

/**
 * This thing will add all common things to each request
 * 
 * @author sergey.k
 * 
 */
public class AllControllerActionsInterceptor extends ControllerBase implements HandlerInterceptor {
	private Logger log = Logger.getLogger(getClass());

	@Autowired
	private LocaleContextResolver localeContextResolver;
	@Autowired
	private SecurityContextResolver<User> securityContextResolver;

	private Gson gson = new Gson();

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		if (handler instanceof ResourceHttpRequestHandler) {
			// just resource request
			return true;
		}

		if (log.isDebugEnabled()) {
			log.debug("Pre: " + request.getRequestURI());
		}
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {

		if (handler instanceof ResourceHttpRequestHandler) {
			return;
		}

		if (log.isDebugEnabled()) {
			log.debug("Post: " + request.getRequestURI());
		}

		if ((modelAndView == null) || (modelAndView.getModel() == null)) {
			return;
		}

		modelAndView.getModel().put("staticResourcesBase", request.getContextPath());
		modelAndView.getModel().put("contextPath", request.getContextPath());
		modelAndView.getModel().put("lang", getLang(request));

		User user = findCurrentUser();
		modelAndView.getModel().put(ATTR_CURRENT_USER, user);
		modelAndView.getModel().put(ATTR_CURRENT_USER + "Json", user == null ? "{}" : gson.toJson(user));
		List<String> roles = securityContextResolver.getCurrentUserGlobalPermissions().stream()
				.map(x -> x.getAuthority()).collect(Collectors.toList());
		modelAndView.getModel().put(ATTR_CURRENT_USER + "RolesJson", user == null ? "[]" : gson.toJson(roles));
	}

	private User findCurrentUser() {
		try {
			return securityContextResolver.getUser();
		} catch (CurrentUserNotFoundException tsue) {
			return null;
		}
	}

	private String getLang(HttpServletRequest request) {
		Locale locale = localeContextResolver.resolveLocale(request);
		return locale != null ? locale.getLanguage() : "en";
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		// do nothing
	}
}
