package org.kissfarmops.shared.config.api;

import java.io.Serializable;

import org.kissfarmops.shared.api.EnvVars;

import lombok.Data;

@Data
public class AppPrototypeConfig implements Serializable {
	private static final long serialVersionUID = -7101149613171344934L;

	private String name;
	private EnvVars env;
}
