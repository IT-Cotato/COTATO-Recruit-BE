package org.cotato.backend.recruit.admin.controller.applicationView;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.cotato.backend.recruit.admin.dto.request.applicationView.PassStatusChangeRequest;
import org.cotato.backend.recruit.admin.service.passStatus.PassStatusChangeService;
import org.cotato.backend.recruit.common.response.ApiResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "지원서 합격 여부 변경 API", description = "지원서 합격 여부 변경 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/application")
public class ApplicationPassStatusController {

	private final PassStatusChangeService passStatusChangeService;

	@Operation(summary = "지원서 합격 여부 변경", description = "지원서의 합격 여부를 변경합니다.")
	@PostMapping("/{applicationId}/pass-status")
	public ApiResponse<Void> updatePassStatus(
			@PathVariable("applicationId") Long applicationId, @Valid @RequestBody PassStatusChangeRequest request) {
		passStatusChangeService.updatePassStatus(applicationId, request);
		return ApiResponse.success();
	}
}
