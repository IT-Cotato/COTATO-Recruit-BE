package org.cotato.backend.recruit.admin.loadtest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.cotato.backend.recruit.common.response.ApiResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 부하 테스트용 Controller DB Connection Pool 고갈 테스트를 위한 API */
@Tag(name = "부하 테스트 API", description = "Connection Pool 고갈 테스트용 API")
@RestController
@RequestMapping("/api/load-test")
@RequiredArgsConstructor
public class LoadTestController {

	private final LoadTestService loadTestService;

	@Operation(summary = "부하 테스트 실행", description = "약 2000자의 콘텐츠를 DB에 저장하고 1초간 Connection을 점유합니다. (Connection Pool 고갈 테스트용)")
	@PostMapping
	public ApiResponse<LoadTestResponse> executeLoadTest(@RequestBody LoadTestRequest request) {
		LoadTestResponse response = loadTestService.processLoadTest(request);
		return ApiResponse.success(response);
	}
}
