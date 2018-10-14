package org.kissfarm.agent.node_identity.impl;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import org.kissfarm.agent.action_executor.api.ActionExecutionFactory;
import org.kissfarm.agent.action_executor.api.ActionExecutionSpi;
import org.kissfarm.agent.action_executor.api.ActionInvocationInfo;
import org.kissfarm.agent.action_executor.api.ActionsExecutionListener;
import org.kissfarm.agent.action_executor.impl_folder.ActionExecutionSpiImpl;
import org.kissfarm.shared.api.NodeIdentity;
import org.kissfarm.shared.config.dto.ActionCommands;
import org.kissfarm.shared.config.dto.ActionStatus;
import org.kissfarm.shared.tools.IdTools;

import com.google.common.base.Preconditions;
import com.google.common.io.Files;

/**
 * This impl will invoke action. Underlying command can either write result
 * {@link NodeIdentity} to result file identified by env variable
 * {@link ActionExecutionSpiImpl#ENV_VAR_FILE_NAME_RESULT} or to std output
 * 
 * @author Sergey Karpushin
 *
 */
public class SelfIdentificationActionBasedImpl extends SelfIdentificationAbstract {
	private ActionExecutionFactory actionExecutionFactory;
	private String[] cmd;
	private String cmdWorkingDirectory;

	public SelfIdentificationActionBasedImpl(ActionExecutionFactory actionExecutionFactory, String[] cmd,
			String cmdWorkingDirectory) {
		this.actionExecutionFactory = actionExecutionFactory;
		this.cmd = cmd;
		this.cmdWorkingDirectory = cmdWorkingDirectory;

		Preconditions.checkArgument(actionExecutionFactory != null, "actionExecutionFactory required");
		Preconditions.checkArgument(cmd != null && cmd.length > 0, "cmd required");
	}

	@Override
	public NodeIdentity resolve() {
		try {
			IdentiyResolverListener listener = new IdentiyResolverListener();
			actionExecutionFactory.startNewInvocation(build(), listener);
			// TBD: We'd better keep trying, don't just stuck or die completely
			String idenityStr = listener.future.join();
			NodeIdentity ret = gson.fromJson(idenityStr, NodeIdentity.class);
			fillEmptyFieldsIfAny(ret);
			if (log.isDebugEnabled()) {
				log.debug("returning NodeIdentity: " + gson.toJson(ret));
			}
			return ret;
		} catch (Throwable t) {
			throw new RuntimeException("Failed to resolve node identity using action: " + Arrays.toString(cmd), t);
		}
	}

	private ActionInvocationInfo build() {
		ActionInvocationInfo ret = new ActionInvocationInfo();
		ret.setName("SelfIdentificationActionBasedImpl");
		ret.setExecutionId(IdTools.randomId());
		ret.setScriptsFolder(cmdWorkingDirectory);
		ret.setInstanceFolder(Files.createTempDir().getAbsolutePath());
		ActionCommands actionCommands = new ActionCommands();
		actionCommands.setInvoke(cmd);
		ret.setActionCommands(actionCommands);
		return ret;
	}

	private class IdentiyResolverListener implements ActionsExecutionListener {
		CompletableFuture<String> future = new CompletableFuture<>();

		@Override
		public void onActionStatusChanged(ActionExecutionSpi actionExecutionSpi, ActionStatus oldStatus,
				ActionStatus newStatus) {
			if (!ActionExecutionSpi.statusesWhenActionCompleted.contains(newStatus)) {
				log.debug("Ignoring status: {}", newStatus);
				return;
			}

			String output = actionExecutionSpi.getOutput();
			if (newStatus == ActionStatus.Success) {
				String result = actionExecutionSpi.getResult();
				if (log.isDebugEnabled()) {
					log.debug("NodeIdentity script finished. Result: {}. Output: {}", result, output);
				}
				future.complete(result != null ? result : output);
			} else {
				future.completeExceptionally(new RuntimeException(
						"NodeIdentity resolution script failed. Status " + newStatus + ". Output: " + output));
			}
		}
	};

}
