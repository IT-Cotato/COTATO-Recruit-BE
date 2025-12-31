package org.cotato.backend.recruit.common.response;

import java.util.List;

public record PageResponse<T>(
		int page, int size, int totalElements, int totalPages, List<T> content) {
	public static <T> PageResponse<T> of(
			int page, int size, int totalElements, int totalPages, List<T> content) {
		return new PageResponse<>(page, size, totalElements, totalPages, content);
	}
}
