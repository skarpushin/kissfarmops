package org.kissfarm.agent.action_executor.impl_folder;

import org.kissfarm.agent.action_executor.api.ActionExecutionSpi;
import org.kissfarm.agent.action_executor.api.ActionStatus;
import org.kissfarm.agent.action_executor.api.ActionsExecutionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Impl that prints changes to log. That's it.
 * 
 * @author Sergey Karpushin
 *
 */
public class ActionsExecutionListenerLoggingImpl implements ActionsExecutionListener {
	private static Logger log = LoggerFactory.getLogger(ActionsExecutionListenerLoggingImpl.class);

	@Override
	public void onActionStatusChanged(ActionExecutionSpi actionExecutionSpi, ActionStatus oldStatus,
			ActionStatus newStatus) {
		log.trace("Action {} status changed from {} to {}", actionExecutionSpi, oldStatus, newStatus);
	}

}
