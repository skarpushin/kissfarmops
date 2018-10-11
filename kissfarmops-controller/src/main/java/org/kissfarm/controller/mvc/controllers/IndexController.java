package org.kissfarm.controller.mvc.controllers;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.summerb.approaches.security.api.Roles;
import org.summerb.approaches.security.api.SecurityContextResolver;
import org.summerb.approaches.springmvc.controllers.ControllerBase;
import org.summerb.approaches.springmvc.security.SecurityConstants;
import org.summerb.approaches.springmvc.utils.AbsoluteUrlBuilder;
import org.summerb.microservices.users.api.dto.User;

import springfox.documentation.annotations.ApiIgnore;

@Controller
@ApiIgnore
public class IndexController extends ControllerBase {
	public static final String DASHBOARD_ROOT = "/web/dashboard";
	public static final String PATH_AGENT_AUTH_TOKENS = "/web/agent-auth-token";
	public static final String PATH_NODES = "/web/node";
	private static final String PATH_FARM_CONFIG = "/web/farm-config";

	@Autowired
	private SecurityContextResolver<User> securityContextResolver;
	@Autowired
	private RedirectStrategy redirectStrategy;
	@Autowired
	private AbsoluteUrlBuilder absoluteUrlBuilder;

	@GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
	public String getIndexPage(Model model, HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		if (!securityContextResolver.hasRole(Roles.ROLE_USER)) {
			// NOTE: This is not 100% accurate. securityViewNamesProvider returns view
			// names. We assumming controller is mapped to the same path
			String redirectTo = absoluteUrlBuilder.buildExternalUrl("/login/form");
			redirectStrategy.sendRedirect(request, response, redirectTo);
			return null;
		}

		redirectStrategy.sendRedirect(request, response, pathForDefaultDashboard());
		return null;
	}

	@Secured(SecurityConstants.ROLE_USER)
	@GetMapping(value = DASHBOARD_ROOT)
	public String getDefaultDashboardPage(Model model) {
		return "web/dashboard/default";
	}

	public static String pathForDefaultDashboard() {
		return DASHBOARD_ROOT;
	}

	@Secured(SecurityConstants.ROLE_USER)
	@GetMapping(value = PATH_AGENT_AUTH_TOKENS)
	public String agentAuthTokensTable() {
		return PATH_AGENT_AUTH_TOKENS;
	}

	@Secured(SecurityConstants.ROLE_USER)
	@GetMapping(value = PATH_NODES)
	public String nodesTable() {
		return PATH_NODES;
	}

	@Secured(SecurityConstants.ROLE_USER)
	@GetMapping(value = PATH_FARM_CONFIG)
	public String farmConfig() {
		return PATH_FARM_CONFIG;
	}
}
