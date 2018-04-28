package org.kissfarm.controller.services.dashboard.rest.v1.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.summerb.approaches.springmvc.controllers.ControllerBase;
import org.summerb.approaches.springmvc.security.SecurityConstants;

@RestController
@Secured(SecurityConstants.ROLE_USER)
@RequestMapping(path = "/rest/api/v1/dashboard")
public class DashboardDataRestController extends ControllerBase {
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	
}
