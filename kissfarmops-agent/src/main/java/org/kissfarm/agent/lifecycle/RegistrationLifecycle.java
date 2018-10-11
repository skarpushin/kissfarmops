package org.kissfarm.agent.lifecycle;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.kissfarm.agent.client.api.ControllerConnection;
import org.kissfarm.agent.client.api.ControllerConnectionInfo;
import org.kissfarm.agent.client.api.ControllerConnectionInfoHolder;
import org.kissfarm.agent.node_identity.api.NodeIdentityHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.StringUtils;
import org.summerb.approaches.springmvc.security.dto.LoginParams;
import org.summerb.approaches.validation.FieldValidationException;
import org.summerb.utils.exceptions.ExceptionUtils;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;

/**
 * Goal of this lifecycle is to obtain registration info, particularly login
 * credentials and make sure they are valid and we have up-to-date session
 * cookie we can use for interaction with controller
 * 
 * @author Sergey Karpushin
 *
 */
public class RegistrationLifecycle extends LifecycleSyncBase implements ControllerConnectionInfoHolder {
	private NodeIdentityHolder nodeIdentityHolder;
	private Gson gson;
	private String filePathName;
	private SelfIdLifecycle selfIdLifecycle;
	private WsConnectLifecycle wsConnectLifecycle;
	private ControllerConnection controllerConnection;
	private String defaultBaseUrl;
	private String defaultAuthToken;

	private ControllerConnectionInfo result;

	protected Lifecycle doStep() throws Exception {
		if (nodeIdentityHolder.getNodeIdentity() == null) {
			return selfIdLifecycle; // fall-back
		}

		File file = new File(filePathName);
		readControllerConnectionInfo(file);

		if (result.getLoginParams() != null) {
			try {
				controllerConnection.assertLogin(result.getLoginParams());
				return wsConnectLifecycle;
			} catch (Throwable t) {
				FieldValidationException fve = ExceptionUtils.findExceptionOfType(t, FieldValidationException.class);
				if (fve != null) {
					log.warn("Login credentials weren't accepted. Will try to re-register", t);
					result.setLoginParams(null);
				} else {
					throw new RuntimeException("Failed to verify password due to an unexpected issue", t);
				}
			}
		}

		// ok. If we're here it means we need to register
		Preconditions.checkState(StringUtils.hasText(result.getAuthToken()),
				"ControllerConnectionInfo.authToken required for registration");
		LoginParams obtainedPassword = controllerConnection.register(nodeIdentityHolder.getNodeIdentity(),
				result.getAuthToken());
		result.setLoginParams(obtainedPassword);
		try {
			FileUtils.writeStringToFile(file, gson.toJson(result), "UTF-8");
		} catch (IOException ie) {
			log.warn("Failed to persist password. Agent will have to go through registration procedure again next time",
					ie);
		}

		controllerConnection.assertLogin(result.getLoginParams());
		return wsConnectLifecycle;
	}

	private void readControllerConnectionInfo(File file) throws IOException {
		if (result != null) {
			// it was already initialized
			return;
		}

		if (!file.exists()) {
			Preconditions.checkState(StringUtils.hasText(defaultBaseUrl) && StringUtils.hasText(defaultAuthToken),
					"ControllerConnectionInfo file (%s) is missing and default values for defaultBaseUrl, defaultAuthToken are not provided. Can't continue",
					file);
			result = new ControllerConnectionInfo();
			result.setBaseUrl(defaultBaseUrl);
			result.setAuthToken(defaultAuthToken);
		} else {
			result = gson.fromJson(FileUtils.readFileToString(file, "UTF-8"), ControllerConnectionInfo.class);
			Preconditions.checkState(result != null, "ControllerConnectionInfo must not be null");
			Preconditions.checkState(StringUtils.hasText(result.getBaseUrl()),
					"ControllerConnectionInfo.baseUrl required");
		}
	}

	@Override
	public ControllerConnectionInfo getControllerConnectionInfo() {
		return result;
	}

	@Autowired
	public void setNodeIdentityHolder(NodeIdentityHolder nodeIdentityHolder) {
		this.nodeIdentityHolder = nodeIdentityHolder;
	}

	@Autowired
	public void setGson(Gson gson) {
		this.gson = gson;
	}

	@Required
	public void setFilePathName(String filePathName) {
		this.filePathName = filePathName;
	}

	@Autowired
	public void setSelfIdLifecycle(SelfIdLifecycle selfIdLifecycle) {
		this.selfIdLifecycle = selfIdLifecycle;
	}

	@Autowired
	public void setWsConnectLifecycle(WsConnectLifecycle wsConnectLifecycle) {
		this.wsConnectLifecycle = wsConnectLifecycle;
	}

	@Autowired
	public void setControllerConnection(ControllerConnection controllerConnection) {
		this.controllerConnection = controllerConnection;
	}

	public void setDefaultBaseUrl(String defaultBaseUrl) {
		this.defaultBaseUrl = defaultBaseUrl;
	}

	public void setDefaultAuthToken(String defaultAuthToken) {
		this.defaultAuthToken = defaultAuthToken;
	}
}
