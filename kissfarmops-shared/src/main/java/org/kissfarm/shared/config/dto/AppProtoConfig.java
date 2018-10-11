package org.kissfarm.shared.config.dto;

import java.io.Serializable;

/**
 * Application Prototype Config
 * 
 * @author Sergey Karpushin
 *
 */
public class AppProtoConfig implements Serializable {
	private static final long serialVersionUID = -7101149613171344934L;

	private String name;
	private EnvVars env;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public EnvVars getEnv() {
		return env;
	}

	public void setEnv(EnvVars env) {
		this.env = env;
	}
}
