package org.kissfarmops.shared.config.api;

import java.io.Serializable;
import java.util.List;

import org.kissfarmops.shared.actions.api.ActionCommands;

import lombok.Data;

@Data
public class ActionConfig implements Serializable {
	private static final long serialVersionUID = 523263258527605358L;

	private String name;
	private List<String> parameterNames;
	private ActionCommands commands;
}
