package org.kissfarmops.agent.application.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.kissfarmops.agent.action_executor.api.ActionExecutionFactory;
import org.kissfarmops.agent.action_executor.api.ActionExecutionSpi;
import org.kissfarmops.agent.action_executor.api.ActionStatus;
import org.kissfarmops.agent.action_executor.api.ActionsExecutionListener;
import org.kissfarmops.agent.action_executor.impl_folder.ActionInvocationInfo;
import org.kissfarmops.agent.application.api.ActionFoldersResolver;
import org.kissfarmops.agent.application.api.AppInstanceSpi;
import org.kissfarmops.agent.application.api.AppListener;
import org.kissfarmops.agent.utils.StringUtils;
import org.kissfarmops.shared.api.EnvVars;
import org.kissfarmops.shared.config.api.ActionConfig;
import org.kissfarmops.shared.config.api.AppDefinitionConfig;
import org.kissfarmops.shared.config.api.AppPrototypeConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

/**
 * Instance of this calss is to represent an application of certain version of
 * the configuration
 * 
 * TODO: 1 thing I don't like about this impl it's tied to an idea that action
 * has to happen in files system. It would be nice to refactor it some day so
 * that actions could be embedded and handled by the agent itself
 * 
 * @author Sergey Karpushin
 *
 */
public class AppInstanceSpiImpl implements AppInstanceSpi {
	private static Logger log = LoggerFactory.getLogger(AppInstanceSpiImpl.class);

	private String version;
	private AppDefinitionConfig definitionConfig;
	private AppPrototypeConfig prototypeConfig;
	private ActionFoldersResolver actionFoldersResolver;
	private AppListener appListener;
	private ActionExecutionFactory actionExecutionFactory;

	private Object syncRoot = new Object();
	private List<ActionExecutionSpi> actionExecutions = new ArrayList<>();
	private List<Runnable> callAfterAllActionsComplete = new LinkedList<>();
	private volatile boolean suspending;

	public AppInstanceSpiImpl(String version, AppDefinitionConfig definitionConfig, AppPrototypeConfig prototypeConfig,
			AppListener appListener, ActionFoldersResolver actionFoldersResolver,
			ActionExecutionFactory actionExecutionFactory) {
		this.actionExecutionFactory = actionExecutionFactory;
		Preconditions.checkArgument(StringUtils.hasText(version), "version must not be empty");
		Preconditions.checkArgument(definitionConfig != null, "definitionConfig must not be null");
		Preconditions.checkArgument(prototypeConfig != null, "prototypeConfig must not be null");
		Preconditions.checkArgument(appListener != null, "appListener must not be null");
		Preconditions.checkArgument(actionFoldersResolver != null, "actionFoldersResolver must not be null");
		Preconditions.checkArgument(actionExecutionFactory != null, "actionExecutionFactory must not be null");

		this.version = version;
		this.definitionConfig = definitionConfig;
		this.prototypeConfig = prototypeConfig;
		this.actionFoldersResolver = actionFoldersResolver;
		this.appListener = appListener;

		reconcileActionsIfAny();
	}

	/**
	 * If instance folder eixsts -> means action is still "running"
	 */
	private void reconcileActionsIfAny() {
		if (definitionConfig.getActions() == null || definitionConfig.getActions().size() == 0) {
			log.warn("No actions provided for application {}", definitionConfig.getName());
			return;
		}

		for (String actionName : definitionConfig.getActions().keySet()) {
			String actionFolder = actionFoldersResolver.resolveInstanceFolder(definitionConfig, prototypeConfig,
					actionName);
			File actionFolderObj = new File(actionFolder);
			if (!actionFolderObj.exists()) {
				continue;
			}

			try {
				synchronized (syncRoot) {
					ActionExecutionSpi executionSpi = actionExecutionFactory.reconcileExistingAction(actionFolder,
							actionsExecutionListener);
					actionExecutions.add(executionSpi);
				}
			} catch (Throwable t) {
				boolean folderWasDeleted = FileUtils.deleteQuietly(actionFolderObj);
				log.error("Failed to reconcile action from instance folder: " + actionName + ". Folder deleted: "
						+ folderWasDeleted, t);
			}
		}
	}

	private ActionsExecutionListener actionsExecutionListener = new ActionsExecutionListener() {
		@Override
		public void onActionStatusChanged(ActionExecutionSpi actionExecutionSpi, ActionStatus oldStatus,
				ActionStatus newStatus) {
			synchronized (syncRoot) {
				try {
					appListener.onActionStatusChanged(AppInstanceSpiImpl.this, actionExecutionSpi, newStatus);
				} catch (Throwable t) {
					log.error("Failed to notify appListener regarding action " + actionExecutionSpi.getName()
							+ " status change to " + newStatus, t);
					// no reason to propagate exception, actionSpi doesn't need to suffer from
					// upstream
				}

				if (ActionExecutionSpi.statusesWhenActionCompleted.contains(newStatus)) {
					actionExecutions.remove(actionExecutionSpi);
					cleanUpInstanceFolder(actionExecutionSpi);
					notifyCallbacksIfAny();
				}
			}
		}

		private void cleanUpInstanceFolder(ActionExecutionSpi actionExecutionSpi) {
			String actionFolder = actionFoldersResolver.resolveInstanceFolder(definitionConfig, prototypeConfig,
					actionExecutionSpi.getName());
			File actionFolderObj = new File(actionFolder);
			if (!FileUtils.deleteQuietly(actionFolderObj)) {
				log.warn("Failed to delete action instance folder after completion: " + actionFolder);
			}
		}

		private void notifyCallbacksIfAny() {
			if (!actionExecutions.isEmpty()) {
				return;
			}
			try {
				callAfterAllActionsComplete.forEach(x -> x.run());
			} catch (Throwable t) {
				log.error("Failed to notify callback after all actions are completed for application "
						+ definitionConfig.getName(), t);
				// no reason to propagate exception
			}
			callAfterAllActionsComplete.clear();
		}
	};

	@Override
	public String getVersion() {
		return version;
	}

	@Override
	public AppDefinitionConfig getAppDefinitionConfig() {
		return definitionConfig;
	}

	@Override
	public AppPrototypeConfig getAppPrototypeConfig() {
		return prototypeConfig;
	}

	@Override
	public void callWhenNoActionsAreBeingExecuted(Runnable callback) {
		synchronized (syncRoot) {
			if (actionExecutions.isEmpty()) {
				callback.run();
				return;
			} else {
				callAfterAllActionsComplete.add(callback);
			}
		}
	}

	@Override
	public ActionExecutionSpi invokeAction(String actionName, String executionId, Map<String, String> parameters) {
		try {
			ActionInvocationInfo invocationInfo = buildActionInvocationInfo(actionName, executionId, parameters);
			Preconditions.checkState(!new File(invocationInfo.getInstanceFolder()).exists(),
					"Can't invoke action %s because previous executond either is not finished or not cleaned up yet: %s",
					invocationInfo.getInstanceFolder());

			if (suspending) {
				return actionExecutionFactory.postponeAction(invocationInfo, actionsExecutionListener);
			}

			synchronized (syncRoot) {
				ActionExecutionSpi newActionExecution = actionExecutionFactory.startNewInvocation(invocationInfo,
						actionsExecutionListener);
				actionExecutions.add(newActionExecution);
				return newActionExecution;
			}
		} catch (Throwable t) {
			throw new RuntimeException("Failed to invokeAction " + actionName, t);
		}
	}

	protected ActionInvocationInfo buildActionInvocationInfo(String actionName, String executionId,
			Map<String, String> parameters) {
		ActionConfig action = definitionConfig.getActions().get(actionName);
		Preconditions.checkArgument(action != null, "Action not found: %s", actionName);

		Map<String, String> envVars = buildEnvVars(parameters);
		String scriptsFolder = actionFoldersResolver.resolveScriptsFolder(definitionConfig, prototypeConfig,
				actionName);
		String instanceFolder = actionFoldersResolver.resolveInstanceFolder(definitionConfig, prototypeConfig,
				actionName);

		return new ActionInvocationInfo(actionName, executionId, scriptsFolder, action.getCommands(), instanceFolder,
				envVars);
	}

	protected Map<String, String> buildEnvVars(Map<String, String> parameters) {
		Map<String, String> envVars = new EnvVars();
		if (definitionConfig.getEnv() != null) {
			envVars.putAll(definitionConfig.getEnv());
		}
		if (prototypeConfig.getEnv() != null) {
			envVars.putAll(prototypeConfig.getEnv());
		}
		if (parameters != null) {
			envVars.putAll(parameters);
		}
		return envVars;
	}

	@Override
	public void suspend() {
		suspending = true;

		synchronized (syncRoot) {
			if (actionExecutions.isEmpty()) {
				return;
			}

			List<ActionExecutionSpi> toSuspend = new ArrayList<>(actionExecutions);
			toSuspend.forEach(action -> {
				try {
					action.suspend();
					// NOTE: if suspend success then it will be removed from actionExecutions
					// naturally upon invocation of actionsExecutionListener
				} catch (Throwable t) {
					log.error("Failed to suspend action " + action, t);
					actionExecutions.remove(action);
				}
			});
		}
	}
}
