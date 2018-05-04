package org.kissfarm.shared.config.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class AppPrototypeConfig implements Serializable {
	private static final long serialVersionUID = -7101149613171344934L;

	private String name;
	private EnvVars env;
}
