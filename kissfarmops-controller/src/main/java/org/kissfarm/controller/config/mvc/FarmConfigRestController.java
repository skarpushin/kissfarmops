package org.kissfarm.controller.config.mvc;

import org.kissfarm.controller.config.api.GitConfig;
import org.kissfarm.controller.config.smachine.dtos.PullConfigUpdateRequest;
import org.kissmachine.api.dto.SmData;
import org.kissmachine.api.machine.StateMachine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.summerb.approaches.jdbccrud.api.EasyCrudValidationStrategy;
import org.summerb.approaches.security.api.dto.ScalarValue;
import org.summerb.approaches.springmvc.controllers.ControllerBase;
import org.summerb.approaches.validation.FieldValidationException;

import com.google.common.base.Preconditions;

@RestController
public class FarmConfigRestController extends ControllerBase {
	@Autowired
	private StateMachine farmConfigStateMachine;
	@Autowired
	private EasyCrudValidationStrategy<GitConfig> gitConfigValidationStrategy;

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
}
