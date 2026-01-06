package org.cotato.backend.recruit.admin.controller.passerManagement;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.cotato.backend.recruit.admin.dto.response.passer.PassStatusSummaryResponse;
import org.cotato.backend.recruit.admin.service.passerManagement.PasserSummaryService;
import org.cotato.backend.recruit.common.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PasserController {

	private final PasserSummaryService passerService;

	/**
	 * 합격자 현황 조회
	 *
	 * @param generationId 기수 ID
	 * @return 합격자 현황 목록
	 */
	@GetMapping("/api/admin/pass-status")
	public ResponseEntity<ApiResponse<List<PassStatusSummaryResponse>>> getPassStatus(
			@RequestParam(name = "generation", required = true) Long generationId) {
		List<PassStatusSummaryResponse> response = passerService.getPasserSummary(generationId);
		return ResponseEntity.ok(ApiResponse.success(response));
	}
}
