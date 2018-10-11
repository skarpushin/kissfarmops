package org.kissmachine.api.state;

import org.kissmachine.api.dto.SmStateData;
import org.kissmachine.api.machine.StateMachine;

public class InitStateAction {
	private SmStateData stateData;
	private StateMachine stateMachine;

	public InitStateAction() {
	}

	public InitStateAction(SmStateData stateData, StateMachine stateMachine) {
		super();
		this.stateData = stateData;
		this.stateMachine = stateMachine;
	}

	public SmStateData getStateData() {
		return stateData;
	}

	public void setStateData(SmStateData stateData) {
		this.stateData = stateData;
	}

	public StateMachine getStateMachine() {
		return stateMachine;
	}

	public void setStateMachine(StateMachine stateMachine) {
		this.stateMachine = stateMachine;
	}
}
