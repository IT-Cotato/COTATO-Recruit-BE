package org.cotato.backend.recruit.common.util;

import java.nio.charset.StandardCharsets;

public class ByteManager {

	public static boolean isExceeding(String content, int limit) {
		if (content == null) {
			return false;
		}
		return getByteSize(content) > limit;
	}

	public static int getByteSize(String content) {
		if (content == null) {
			return 0;
		}
		return content.getBytes(StandardCharsets.UTF_8).length;
	}
}
