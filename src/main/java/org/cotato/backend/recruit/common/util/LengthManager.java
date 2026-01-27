package org.cotato.backend.recruit.common.util;

public class LengthManager {

	public static boolean isExceeding(String content, int limit) {
		if (content == null) {
			return false;
		}
		return getCharacterCount(content) > limit;
	}

	public static int getCharacterCount(String content) {
		if (content == null) {
			return 0;
		}
		return content.length();
	}
}
