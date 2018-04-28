package org.kissfarmops.shared.api;

import org.apache.commons.lang3.RandomStringUtils;

public class IdTools {

	/**
	 * @return Case-sensitive random string
	 */
	public static String randomId() {
		return RandomStringUtils.randomAlphanumeric(8);

		/*
		 * UUID ret = UUID.randomUUID(); return
		 * Long.toString(ret.getMostSignificantBits(), Character.MAX_RADIX) +
		 * Long.toString(ret.getLeastSignificantBits(), Character.MAX_RADIX);
		 */
	}
}
