package org.cotato.backend.recruit.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

	private String code;
	private String message;
	private T data;

	// 성공 응답 (데이터 있음)
	public static <T> ApiResponse<T> success(T data) {
		return new ApiResponse<>("SUCCESS", "요청이 성공적으로 처리되었습니다.", data);
	}

	// 성공 응답 (데이터 없음)
	public static <T> ApiResponse<T> success() {
		return new ApiResponse<>("SUCCESS", "요청이 성공적으로 처리되었습니다.", null);
	}

	// 실패 응답
	public static <T> ApiResponse<T> error(String code, String message) {
		return new ApiResponse<>(code, message, null);
	}

	// 실패 응답 (데이터 포함)
	public static <T> ApiResponse<T> error(String code, String message, T data) {
		return new ApiResponse<>(code, message, data);
	}
}
