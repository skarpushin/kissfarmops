package org.kissfarmops.shared.api;

import org.summerb.approaches.validation.ValidationError;

public class NodeTagsRequiredValidationError extends ValidationError {
	private static final long serialVersionUID = -1914777243895545989L;

	@SuppressWarnings("deprecation")
	public NodeTagsRequiredValidationError() {
		super("node.tags.required", "node.tags");
	}
}
