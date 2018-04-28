package org.kissfarm.controller.mvc.controllers;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.kissfarm.controller.services.dashboard.mvc.controllers.DashboardWebController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.summerb.approaches.security.api.Roles;
import org.summerb.approaches.security.api.SecurityContextResolver;
import org.summerb.approaches.springmvc.controllers.ControllerBase;
import org.summerb.approaches.springmvc.security.apis.SecurityViewNamesProvider;
import org.summerb.approaches.springmvc.utils.AbsoluteUrlBuilder;
import org.summerb.microservices.users.api.PermissionService;
import org.summerb.microservices.users.api.dto.User;

import springfox.documentation.annotations.ApiIgnore;

@Controller
@ApiIgnore
public class IndexController extends ControllerBase {
	@Autowired
	public PermissionService permissionService;
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

		redirectStrategy.sendRedirect(request, response, DashboardWebController.pathForDefaultDashboard());
		return null;
	}
}
