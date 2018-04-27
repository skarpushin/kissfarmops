package org.kissfarmops.agent.utils;

/**
 * I don't want to import Spring here. Yet. But I got used to couple things from
 * it.
 * 
 * @author Sergey Karpushin
 *
 */
public class StringUtils {

	public static boolean hasText(String str) {
		if (str == null || str.length() == 0 || str.trim().length() == 0) {
			return false;
		}

		return true;
	}
}
