package org.kissmachine.api.dto;

import java.io.Serializable;

import org.summerb.approaches.jdbccrud.api.dto.HasAuthor;
import org.summerb.approaches.jdbccrud.api.dto.HasTimestamps;
import org.summerb.approaches.jdbccrud.api.dto.HasUuid;

/**
 * Data of 1 state instance. It also contains information about transition to
 * the next state
 * 
 * @author Sergey Karpushin
 */
public class SmStateData implements HasUuid, HasTimestamps, HasAuthor {
	private static final long serialVersionUID = -8091893459546262874L;

	public static final String FN_PARAMS = "params";
	public static final String FN_STATE = "state";
	public static final String FN_RESULT = "result";

	public static final String FN_MACHINE_TYPE = SmData.FN_MACHINE_TYPE;
	public static final String FN_TO_STATE = "toState";

	/**
	 * Unique ID of instance of this state. Every time machine enters this state new
	 * id generated for this state
	 */
	private String id;
	private long createdAt;
	private long modifiedAt;
	private String createdBy;
	private String modifiedBy;

	private String machineType;
	private String machineId;

	/**
	 * This is state name. But it's not really user-friendly name. Think of it as a
	 * variable name in programming language
	 */
	private String stateName;

	/**
	 * Variables used to enter this state, must not be changed
	 */
	private Serializable params;

	/**
	 * Variables used during this state lifecycle
	 */
	private Serializable state;

	/**
	 * Variables used to describe result of this state. It must be set ONLY when
	 * exiting the state
	 */
	private Serializable result;

	/**
	 * Free form message that can be displayed in logs and UI. it could contain
	 * brief description of the result OR exception information
	 */
	private String resultMessage;
	private boolean exception;
	private String toState;
	private String toStateId;

	@Override
	public String getId() {
		return id;
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public long getCreatedAt() {
		return createdAt;
	}

	@Override
	public void setCreatedAt(long createdAt) {
		this.createdAt = createdAt;
	}

	@Override
	public long getModifiedAt() {
		return modifiedAt;
	}

	@Override
	public void setModifiedAt(long modifiedAt) {
		this.modifiedAt = modifiedAt;
	}

	@Override
	public String getCreatedBy() {
		return createdBy;
	}

	@Override
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	@Override
	public String getModifiedBy() {
		return modifiedBy;
	}

	@Override
	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public String getMachineType() {
		return machineType;
	}

	public void setMachineType(String machineType) {
		this.machineType = machineType;
	}

	public String getMachineId() {
		return machineId;
	}

	public void setMachineId(String machineId) {
		this.machineId = machineId;
	}

	public String getStateName() {
		return stateName;
	}

	public void setStateName(String stateName) {
		this.stateName = stateName;
	}

	public Serializable getParams() {
		return params;
	}

	public void setParams(Serializable params) {
		this.params = params;
	}

	public Serializable getState() {
		return state;
	}

	public void setState(Serializable state) {
		this.state = state;
	}

	public Serializable getResult() {
		return result;
	}

	public void setResult(Serializable result) {
		this.result = result;
	}

	public String getResultMessage() {
		return resultMessage;
	}

	public void setResultMessage(String resultMessage) {
		this.resultMessage = resultMessage;
	}

	public boolean isException() {
		return exception;
	}

	public void setException(boolean exception) {
		this.exception = exception;
	}

	public String getToState() {
		return toState;
	}

	public void setToState(String toState) {
		this.toState = toState;
	}

	public String getToStateId() {
		return toStateId;
	}

	public void setToStateId(String toInstanceId) {
		this.toStateId = toInstanceId;
	}

}
