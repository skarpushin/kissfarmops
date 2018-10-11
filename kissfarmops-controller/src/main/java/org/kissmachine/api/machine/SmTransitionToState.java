package org.kissmachine.api.machine;

import org.kissmachine.api.dto.SmStateData;
import org.kissmachine.api.state.SmState;
import org.summerb.approaches.jdbccrud.common.DtoBase;

/**
 * This is a Parameter Object that describes transition to the next state. See
 * {@link SmEventDeliveryStrategy#sendEvent(org.springframework.messaging.Message, StateMachine)}
 * 
 * @author Sergey Karpushin
 *
 */
public class SmTransitionToState implements DtoBase {
	private static final long serialVersionUID = 41068469871643648L;

	/**
	 * Free form message that can be displayed in logs and UI. it could contain
	 * brief description of the resultMessage OR exception information.
	 */
	private String resultMessage;

	/**
	 * True if machine is finished
	 */
	private boolean finished;

	/**
	 * @return state identifier to transition to, see {@link SmState#getName()}
	 */
	private String targetStateName;

	/**
	 * @return variables used to activate next state. Will be transfered to
	 *         {@link SmStateData#getParams()}
	 */
	private DtoBase nextStateParams;

	public SmTransitionToState() {
	}

	public SmTransitionToState(String resultMessage, boolean finished, String targetStateName,
			DtoBase nextStateParams) {
		super();
		this.resultMessage = resultMessage;
		this.finished = finished;
		this.targetStateName = targetStateName;
		this.nextStateParams = nextStateParams;
	}

	public String getResultMessage() {
		return resultMessage;
	}

	public void setResultMessage(String result) {
		this.resultMessage = result;
	}

	public boolean isFinished() {
		return finished;
	}

	public void setFinished(boolean finished) {
		this.finished = finished;
	}

	public String getTargetStateName() {
		return targetStateName;
	}

	public void setTargetStateName(String targetStateName) {
		this.targetStateName = targetStateName;
	}

	public DtoBase getNextStateParams() {
		return nextStateParams;
	}

	public void setNextStateParams(DtoBase nextStateParams) {
		this.nextStateParams = nextStateParams;
	}
}
