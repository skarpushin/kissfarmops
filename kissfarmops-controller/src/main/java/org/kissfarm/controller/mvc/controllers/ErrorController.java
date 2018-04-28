package org.kissfarm.controller.mvc.controllers;

import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.summerb.utils.logging.FeedbackExceptionInfo;
import org.summerb.utils.logging.RollingFileAppenderRecurringExcSkip;

import springfox.documentation.annotations.ApiIgnore;

@Controller
@ApiIgnore
public class ErrorController extends org.summerb.approaches.springmvc.controllers.ErrorController {
	@Secured("ROLE_ADMIN")
	@RequestMapping(method = RequestMethod.GET, value = "/error/exc", produces = MediaType.TEXT_HTML_VALUE)
	public String getExceptionStatistics(
			@RequestParam(name = "throw", required = false, defaultValue = "false") Boolean doThrow, Model model,
			HttpServletRequest request) {
		if (doThrow) {
			throw new RuntimeException("test exception");
		}
		return "common/excstat";
	}

	@Secured("ROLE_ADMIN")
	@RequestMapping(method = RequestMethod.GET, value = "/rest/error/exc", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody List<FeedbackExceptionInfo> ajaxGetExceptionStatistics() {
		if (RollingFileAppenderRecurringExcSkip.getInstance() == null) {
			return new LinkedList<FeedbackExceptionInfo>();
		}

		return RollingFileAppenderRecurringExcSkip.getInstance().getStatistics();
	}
}
