package org.kissmachine.api.dto;

import java.io.Serializable;

import org.summerb.approaches.jdbccrud.api.dto.HasAuthor;
import org.summerb.approaches.jdbccrud.api.dto.HasTimestamps;
import org.summerb.approaches.jdbccrud.api.dto.HasUuid;
import org.summerb.approaches.jdbccrud.common.DtoBase;

/**
 * State Machine Data. This DTO stores machine statefull values which can be
 * serialized/deserialized to the storage
 * 
 * @author Sergey Karpushin
 *
 */
public class SmData implements DtoBase, HasUuid, HasTimestamps, HasAuthor {
	private static final long serialVersionUID = 7568154852931382016L;

	public static final String FN_VARS = "vars";
	public static final String FN_MACHINE_TYPE = "machineType";

	private String machineType;
	/**
	 * Machine instance id
	 */
	private String id;
	private long createdAt;
	private long modifiedAt;
	private String createdBy;
	private String modifiedBy;

	/**
	 * Machine might be associated with a particular subject. Document or other
	 * entity. This field can be used to store this ID and then retrieve machine(s)
	 * related to particular ID
	 */
	private String subjectId;

	/**
	 * Current machine state name
	 */
	private String currentStateName;

	/**
	 * Current machine state instance id
	 */
	private String currentStateId;

	/**
	 * Global machine variables which should be preserved regardless of state
	 * transitioning. Each state can update these variables though
	 */
	private Serializable vars;

	/**
	 * Indicates if state machine finished it's work due to successful completion or
	 * due to error
	 */
	private boolean finished;

	/**
	 * Indicates if machine was finished due to an error
	 */
	private boolean exception;

	public String getMachineType() {
		return machineType;
	}

	public void setMachineType(String machineType) {
		this.machineType = machineType;
	}

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

	public String getSubjectId() {
		return subjectId;
	}

	public void setSubjectId(String subjectId) {
		this.subjectId = subjectId;
	}

	public String getCurrentStateName() {
		return currentStateName;
	}

	public void setCurrentStateName(String stateName) {
		this.currentStateName = stateName;
	}

	public String getCurrentStateId() {
		return currentStateId;
	}

	public void setCurrentStateId(String stateInstanceId) {
		this.currentStateId = stateInstanceId;
	}

	public Serializable getVars() {
		return vars;
	}

	public void setVars(Serializable vars) {
		this.vars = vars;
	}

	public boolean isFinished() {
		return finished;
	}

	public void setFinished(boolean finished) {
		this.finished = finished;
	}

	public boolean isException() {
		return exception;
	}

	public void setException(boolean exception) {
		this.exception = exception;
	}
}
