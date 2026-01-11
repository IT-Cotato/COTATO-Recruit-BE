package org.cotato.backend.recruit.common.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateFormatter {

	private static final DateTimeFormatter MONTH_DAY_FORMATTER =
			DateTimeFormatter.ofPattern("M월 d일");

	private DateFormatter() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * LocalDateTime을 "M월 d일" 형식으로 포맷팅
	 *
	 * @param dateTime 포맷팅할 날짜 시간 (null 가능)
	 * @return 포맷팅된 문자열 또는 null
	 */
	public static String formatMonthDay(LocalDateTime dateTime) {
		if (dateTime == null) {
			return null;
		}
		return dateTime.format(MONTH_DAY_FORMATTER);
	}
}
