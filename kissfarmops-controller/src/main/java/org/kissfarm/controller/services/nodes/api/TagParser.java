package org.kissfarm.controller.services.nodes.api;

import java.util.List;

public interface TagParser {

	List<String> parseTags(String tagsStr);

	String formatTags(List<String> tags);

}