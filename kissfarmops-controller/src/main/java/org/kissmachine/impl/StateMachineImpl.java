package org.kissmachine.impl;

import static org.springframework.util.CollectionUtils.isEmpty;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import org.kissfarm.shared.tools.IdTools;
import org.kissmachine.api.dto.SmData;
import org.kissmachine.api.dto.SmStateData;
import org.kissmachine.api.easycrud.SmDataService;
import org.kissmachine.api.easycrud.SmStateDataService;
import org.kissmachine.api.machine.SmEventDeliveryStrategy;
import org.kissmachine.api.machine.SmTransitionListener;
import org.kissmachine.api.machine.SmTransitionToState;
import org.kissmachine.api.machine.StateMachine;
import org.kissmachine.api.machine.StateMachineKickstarter;
import org.kissmachine.api.state.InitStateAction;
import org.kissmachine.api.state.ResumeStateAction;
import org.kissmachine.api.state.SmState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.summerb.approaches.jdbccrud.api.exceptions.EntityNotFoundException;
import org.summerb.approaches.security.api.exceptions.NotAuthorizedException;
import org.summerb.approaches.validation.FieldValidationException;
import org.summerb.utils.exceptions.ExceptionUtils;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;

/**
 * Default impl for {@link StateMachine}
 * 
 * @author Sergey Karpushin
 *
 */
public class StateMachineImpl
		implements StateMachine, StateMachineKickstarter, BiConsumer<SmTransitionToState, Throwable> {
	private final Logger log = LoggerFactory.getLogger(getClass());

	private String machineType;
	private List<SmState> allStates;
	private SmState initialState;

	private SmTransitionListener transitionListener = new SmTransitionListenerNoOpImpl();
	private SmEventDeliveryStrategy eventDeliveryStrategy = new SmEventDeliveryStrategySyncImpl();
	private SmDataService smDataService;
	private SmStateDataService smStateDataService;

	private SmData machineData;
	private SmState currentState;
	private Throwable exception;
	private Object syncRoot = new Object();

	@Override
	public String getMachineType() {
		return machineType;
	}

	@Override
	public SmData getMachineData() {
		return machineData;
	}

	@Override
	public SmState getCurrentState() {
		return currentState;
	}

	@Override
	public Throwable getMachineExceptionIfAny() {
		return exception;
	}

	@Override
	@Transactional(rollbackFor = Throwable.class)
	public <T> void sendEvent(Message<T> message) {
		CompletableFuture<SmTransitionToState> future = eventDeliveryStrategy.sendEvent(message, this);
		future.whenComplete(this); // "this" as a (BiConsumer<SmTransitionToState, Throwable>)
	}

	// TBD: Wrap it somehow in a transactional context WHEN called from alternative thread
	// TBD: Also set security context OR expect caller to be responsible for that
	@Override
	@Transactional(rollbackFor = Throwable.class)
	public void accept(SmTransitionToState result, Throwable exc) {
		synchronized (syncRoot) {
			try {
				if (exc != null) {
					log.error("Exception in State Machine", exc);
					doAcceptException(exc);
					return;
				}

				doAcceptNextStep(result);
			} catch (Throwable t) {
				log.error("Failed to process state change", t);
				// no re-throwing, there might be no one to catch it because handling might be
				// async
			}
		}
	}

	/**
	 * TBD: This is a VERY dangerous place. If this method fails, state machine
	 * basically dies. We need to figure out how to recover in case of horrible
	 * failure
	 */
	private void doAcceptNextStep(SmTransitionToState result)
			throws FieldValidationException, NotAuthorizedException, EntityNotFoundException {
		if (result == null) {
			return;
		}

		SmState nextState = findStateByName(result.getTargetStateName());
		Preconditions.checkArgument(nextState != null,
				"Wrong transition request from state %s to state %s. Because latter doesn't existing in current state machine %s",
				currentState.getName(), result.getTargetStateName(), machineType);

		long now = System.currentTimeMillis();

		SmStateData newStateData = buildNewState(result, now);
		if (smStateDataService != null) {
			newStateData = smStateDataService.create(newStateData);
		}

		SmStateData curStateData = currentState.getStateData();
		curStateData.setToState(result.getTargetStateName());
		curStateData.setToStateId(newStateData.getId());
		curStateData.setResultMessage(result.getResultMessage());
		if (smStateDataService != null) {
			curStateData = smStateDataService.update(curStateData);
			currentState.setStateData(curStateData);
		}

		machineData.setCurrentStateName(result.getTargetStateName());
		machineData.setCurrentStateId(newStateData.getId());
		// NOTE: We also can expect that Machine variables are changed by now
		if (smDataService != null) {
			machineData = smDataService.update(machineData);
		}

		SmState oldState = currentState;
		currentState = nextState;
		transitionListener.onTransition(this, oldState, nextState);
		sendEvent(MessageBuilder.withPayload(new InitStateAction(newStateData, this)).build());
	}

	private SmStateData buildNewState(SmTransitionToState result, long now) {
		SmStateData newStateData = new SmStateData();
		newStateData.setMachineId(machineData.getId());
		newStateData.setMachineType(machineData.getMachineType());
		newStateData.setId(IdTools.randomId());
		newStateData.setStateName(result.getTargetStateName());
		newStateData.setParams(result.getNextStateParams());
		return newStateData;
	}

	private void doAcceptException(Throwable exc)
			throws FieldValidationException, NotAuthorizedException, EntityNotFoundException {
		machineData.setException(true);
		machineData.setFinished(true);
		this.exception = exc;
		if (smDataService != null) {
			machineData = smDataService.update(machineData);
		}

		SmStateData smStateData = currentState.getStateData();
		smStateData.setException(true);
		smStateData.setResultMessage(ExceptionUtils.getAllMessagesRaw(exc));
		if (smStateDataService != null) {
			smStateData = smStateDataService.update(smStateData);
			currentState.setStateData(smStateData);
		}

		currentState = null;
	}

	private SmState findStateByName(String stateName) {
		return allStates.stream().filter(x -> x.getName().equals(stateName)).findFirst().orElse(null);
	}

	@Override
	@Transactional(rollbackFor = Throwable.class)
	public String startNew(Serializable initialStateParams, Serializable machineVars) throws IllegalStateException {
		synchronized (syncRoot) {
			try {
				Preconditions.checkState(machineData == null,
						"Machine is not supposed to be running when you call resumeExisting()");

				machineData = buildNewMachineData(initialState.getName(), machineVars);
				if (smDataService != null) {
					machineData = smDataService.create(machineData);
				}

				this.currentState = initialState;

				SmStateData smStateData = buildStateData(initialStateParams);
				if (smStateDataService != null) {
					smStateData = smStateDataService.create(smStateData);
				}

				machineData.setCurrentStateId(smStateData.getId());
				if (smDataService != null) {
					machineData = smDataService.update(machineData);
				}

				sendEvent(MessageBuilder.withPayload(new InitStateAction(smStateData, this)).build());

				return machineData.getId();
			} catch (Throwable t) {
				this.exception = t;
				Throwables.throwIfInstanceOf(t, IllegalStateException.class);
				throw new RuntimeException("Failed to start new machine instance " + machineType, t);
			}
		}
	}

	private SmStateData buildStateData(Serializable params) {
		SmStateData ret = new SmStateData();
		ret.setStateName(currentState.getName());
		ret.setId(IdTools.randomId());
		ret.setParams(params);
		ret.setMachineType(machineType);
		ret.setMachineId(machineData.getId());
		return ret;
	}

	private SmData buildNewMachineData(String initiStateName, Serializable machineVars) {
		SmData ret = new SmData();
		ret.setCreatedAt(System.currentTimeMillis());
		ret.setId(IdTools.randomId());
		ret.setVars(machineVars);
		ret.setMachineType(machineType);
		ret.setModifiedAt(ret.getCreatedAt());
		ret.setCurrentStateName(initiStateName);
		return ret;
	}

	@Override
	public void resumeExisting(String instanceId) throws IllegalStateException {
		synchronized (syncRoot) {
			try {
				Preconditions.checkState(machineData == null,
						"Machine is not supposed to be running when you call resumeExisting()");

				SmData smData = loadMachineData(instanceId);
				internalResumeExisting(smData);

				// x.
			} catch (Throwable t) {
				this.exception = t;
				Throwables.throwIfInstanceOf(t, IllegalStateException.class);
				throw new RuntimeException("Failed to resume existing machine instance " + instanceId, t);
			}
		}
	}

	@Override
	public void resumeExisting(SmData existingMachine) {
		synchronized (syncRoot) {
			try {
				Preconditions.checkState(machineData == null,
						"Machine is not supposed to be running when you call resumeExisting()");

				internalResumeExisting(existingMachine);

				// x.
			} catch (Throwable t) {
				this.exception = t;
				Throwables.throwIfInstanceOf(t, IllegalStateException.class);
				throw new RuntimeException("Failed to resume existing machine instance " + existingMachine.getId(), t);
			}
		}
	}

	private void internalResumeExisting(SmData smData) {
		this.machineData = smData;

		SmState smState = getState(smData.getCurrentStateName());
		SmStateData smStateData = loadStateData(smData.getCurrentStateId());

		this.currentState = smState;
		if (smStateData.isException()) {
			this.exception = new RuntimeException(
					smStateData.getResultMessage() != null ? smStateData.getResultMessage() : "Unknown exception");
		} else {
			sendEvent(MessageBuilder.withPayload(new ResumeStateAction(smStateData, this)).build());
		}
	}

	private SmState getState(String stateName) {
		Preconditions.checkState(!isEmpty(allStates), "allStates must be populated");
		SmState smState = allStates.stream().filter(x -> ObjectUtils.nullSafeEquals(stateName, x.getName())).findFirst()
				.orElseThrow(ese("State " + stateName + " not found in state machine " + machineType));
		return smState;
	}

	private SmStateData loadStateData(String stateId) {
		try {
			Preconditions.checkState(smStateDataService != null, "smStateDataService is required for this oiperation");
			SmStateData smStateData = smStateDataService.findById(stateId);
			Preconditions.checkState(smStateData != null, "State %s not found", stateId);
			return smStateData;
		} catch (Throwable t) {
			throw new RuntimeException("Failed to load State data " + stateId, t);
		}
	}

	private SmData loadMachineData(String instanceId) {
		try {
			Preconditions.checkState(smDataService != null, "smDataService is required for this oiperation");
			SmData smData = smDataService.findById(instanceId);
			Preconditions.checkArgument(smData != null, "StateMachine %s instance %s machineData not found",
					machineType, instanceId);
			Preconditions.checkArgument(!smData.isFinished(),
					"StateMachine %s can't resume instance %s because it was already finished", machineType,
					instanceId);
			return smData;
		} catch (Throwable t) {
			throw new RuntimeException("Failed to load State Machine data " + instanceId, t);
		}
	}

	private Supplier<IllegalStateException> ese(String msg) {
		return () -> new IllegalStateException(msg);
	}

	@Override
	@Transactional(rollbackFor = Throwable.class)
	public void stop() {
		synchronized (syncRoot) {
			try {
				Preconditions.checkState(machineData != null, "Can't stop machine that is not initialized");
				Preconditions.checkState(!machineData.isFinished(),
						"Machine must be in progress, can't stop ongoing machine");
				Preconditions.checkState(currentState != null,
						"Can't stop when there is no current state (machine might have been already stopped)");

				currentState.stop();

				if (smStateDataService != null) {
					smStateDataService.update(currentState.getStateData());
				}

				currentState = null;
			} catch (Throwable t) {
				log.error("Failed to stop the machine", t);
				return;
			}
		}
	}

	public void setMachineType(String machineType) {
		this.machineType = machineType;
	}

	public SmTransitionListener getTransitionListener() {
		return transitionListener;
	}

	public void setTransitionListener(SmTransitionListener transitionListener) {
		this.transitionListener = transitionListener;
	}

	public SmEventDeliveryStrategy getEventDeliveryStrategy() {
		return eventDeliveryStrategy;
	}

	public void setEventDeliveryStrategy(SmEventDeliveryStrategy eventDeliveryStrategy) {
		this.eventDeliveryStrategy = eventDeliveryStrategy;
	}

	public SmState getInitialState() {
		return initialState;
	}

	public void setInitialState(SmState initialState) {
		this.initialState = initialState;
	}

	public List<SmState> getAllStates() {
		return allStates;
	}

	public void setAllStates(List<SmState> allStates) {
		this.allStates = allStates;
	}

	public SmDataService getSmDataService() {
		return smDataService;
	}

	public void setSmDataService(SmDataService smDataService) {
		this.smDataService = smDataService;
	}

	public SmStateDataService getSmStateDataService() {
		return smStateDataService;
	}

	public void setSmStateDataService(SmStateDataService smStateDataService) {
		this.smStateDataService = smStateDataService;
	}

}
