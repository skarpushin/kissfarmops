package org.kissfarm.controller.services.nodes.impl;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;
import org.kissfarm.controller.services.nodes.api.TagParser;

public class TagParserImplTest {

	@Test
	public void testParseTags() {
		TagParser f = new TagParserImpl();
		assertArrayEquals(new String[0], f.parseTags("").toArray());
		assertArrayEquals(new String[] { "aa" }, f.parseTags(" aa ").toArray());
		assertArrayEquals(new String[] { "aa", "bb" }, f.parseTags(" aa ,bb").toArray());
	}

	@Test
	public void testFormatTags() {
		TagParser f = new TagParserImpl();

		String result = f.formatTags(Arrays.asList("aa", "bb"));
		assertEquals("aa, bb", result);
	}

}
