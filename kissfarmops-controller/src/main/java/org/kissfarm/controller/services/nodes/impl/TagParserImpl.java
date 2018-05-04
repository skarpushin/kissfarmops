package org.kissfarm.controller.services.nodes.impl;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.kissfarm.controller.services.nodes.api.TagParser;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

public class TagParserImpl implements TagParser {
	private String separator = ",";

	@Override
	public List<String> parseTags(String tagsStr) {
		if (!StringUtils.hasText(tagsStr)) {
			return new LinkedList<>();
		}

		return Arrays.asList(tagsStr.split(separator)).stream().map(x -> x.trim()).filter(x -> x.length() > 0)
				.collect(Collectors.toList());
	}

	@Override
	public String formatTags(List<String> tags) {
		if (CollectionUtils.isEmpty(tags)) {
			return "";
		}

		return tags.stream().reduce((t, u) -> t + ", " + u).get();
	}

	public String getSeparator() {
		return separator;
	}

	public void setSeparator(String separator) {
		this.separator = separator;
	}
}
