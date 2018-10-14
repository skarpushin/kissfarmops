package org.kissfarm.controller.config.mvc;

import java.io.File;
import java.io.FileInputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.kissfarm.controller.config.api.FarmConfigPackager;
import org.kissfarm.controller.config.dto.GitConfig;
import org.kissfarm.controller.config.smachine.dtos.PullConfigUpdateRequest;
import org.kissfarm.controller.security.SecurityConstantsEx;
import org.kissmachine.api.dto.SmData;
import org.kissmachine.api.machine.StateMachine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.summerb.approaches.jdbccrud.api.EasyCrudValidationStrategy;
import org.summerb.approaches.security.api.AuditEvents;
import org.summerb.approaches.security.api.dto.ScalarValue;
import org.summerb.approaches.springmvc.controllers.ControllerBase;
import org.summerb.approaches.springmvc.security.SecurityConstants;
import org.summerb.approaches.springmvc.utils.CurrentRequestUtils;
import org.summerb.approaches.validation.FieldValidationException;
import org.summerb.microservices.articles.mvc.ArticleController.AttachmentNotFoundException;
import org.summerb.utils.exceptions.translator.ExceptionTranslator;

import com.google.common.base.Preconditions;

@RestController
@Secured(value = { SecurityConstants.ROLE_USER, SecurityConstantsEx.ROLE_NODE })
public class FarmConfigRestController extends ControllerBase {
	private static final String REST_API_V1_FARM_CONFIG_PACKAGE = "/rest/api/v1/farm-config/package/";

	@Autowired
	private StateMachine farmConfigStateMachine;
	@Autowired
	private EasyCrudValidationStrategy<GitConfig> gitConfigValidationStrategy;
	@Autowired
	private FarmConfigPackager farmConfigPackager;
	@Autowired
	private ExceptionTranslator exceptionTranslator;
	@Autowired
	private AuditEvents auditEvents;

	@GetMapping(value = "/rest/api/v1/farm-config/sm-data")
	public SmData getCurrentConfig() {
		return farmConfigStateMachine.getMachineData();
	}

	@PutMapping(value = "/rest/api/v1/farm-config/git-config")
	public ScalarValue<String> setCurrentConfig(@RequestBody GitConfig newConfig) throws FieldValidationException {
		Preconditions.checkArgument(newConfig != null, "Config must present");
		gitConfigValidationStrategy.validateForCreate(newConfig);
		farmConfigStateMachine.sendEvent(MessageBuilder.withPayload(newConfig).build());
		return ScalarValue.forV("OK");
	}

	@GetMapping(value = "/rest/api/v1/farm-config/actions/check-updates")
	public ScalarValue<String> checkUpdates() {
		farmConfigStateMachine.sendEvent(MessageBuilder.withPayload(new PullConfigUpdateRequest()).build());
		return ScalarValue.forV("OK");
	}

	public static String getFarmConfigPackageUrlPath(String packageFilename) {
		return REST_API_V1_FARM_CONFIG_PACKAGE + FilenameUtils.getBaseName(packageFilename);
	}

	@RequestMapping(method = RequestMethod.GET, value = REST_API_V1_FARM_CONFIG_PACKAGE + "{packageId}")
	public ResponseEntity<InputStreamResource> getAttachment(@PathVariable("packageId") String packageId,
			HttpServletResponse response) throws AttachmentNotFoundException {
		try {
			if (packageId.contains(".") || packageId.contains("/")) {
				auditEvents.report(AuditEvents.AUDIT_INJECTION_ATTEMPT,
						ScalarValue.forV("HTTP EndPoint (" + REST_API_V1_FARM_CONFIG_PACKAGE
								+ ") hack attempt to alter FarmConfig package path: " + packageId));
				throw new IllegalArgumentException("Are you trying to hack this system? Incident reported.");
			}

			File file = new File(farmConfigPackager.getPackageFullPathByBaseName(packageId));
			Preconditions.checkArgument(file.exists(), "Such package doesn't exist " + packageId);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.parseMediaType("application/zip"));
			headers.setContentLength(file.length());
			response.setHeader("Content-Disposition",
					"attachment; filename=\"" + FilenameUtils.getName(file.getAbsolutePath()) + "\"");

			InputStreamResource ret = new InputStreamResource(new FileInputStream(file));
			return new ResponseEntity<InputStreamResource>(ret, headers, HttpStatus.OK);
		} catch (Throwable t) {
			log.error("Failed to get FarmConfig package", t);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			String msg = exceptionTranslator.buildUserMessage(t, CurrentRequestUtils.getLocale());
			response.setHeader("Error", "Failed to get article attachment -> " + msg);
			return null;
		}
	}
}
