package org.kissfarm.controller.services.dashboard.mvc.controllers;

import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.summerb.approaches.springmvc.controllers.ControllerBase;
import org.summerb.approaches.springmvc.security.SecurityConstants;

@Controller
@RequestMapping(DashboardWebController.DASHBOARD_ROOT)
@Secured(SecurityConstants.ROLE_USER)
public class DashboardWebController extends ControllerBase {
	public static final String DASHBOARD_ROOT = "/web/dashboard";

	@RequestMapping(value = "", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
	public String getDefaultDashboardPage(Model model) {
		return "web/dashboard/default";
	}

	public static String pathForDefaultDashboard() {
		return DASHBOARD_ROOT;
	}
}
