package org.cotato.backend.recruit.admin.controller.recruitmentActive;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.cotato.backend.recruit.admin.dto.request.email.RecruitmentNotificationEmailRequest;
import org.cotato.backend.recruit.admin.dto.request.email.RecruitmentNotificationEmailSendRequest;
import org.cotato.backend.recruit.admin.dto.response.email.RecruitmentEmailSendResponse;
import org.cotato.backend.recruit.admin.dto.response.email.RecruitmentEmailTemplateResponse;
import org.cotato.backend.recruit.admin.service.email.RecruitmentEmailTemplateService;
import org.cotato.backend.recruit.admin.service.email.RecruitmentNotificationEmailSendService;
import org.cotato.backend.recruit.common.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "모집 알림 메일 관리 API", description = "모집 시작 알림 메일 관리 API")
@RestController
@RequestMapping("/api/admin/recruitment-notification-emails")
@RequiredArgsConstructor
public class RecruitmentEmailController {

	private final RecruitmentEmailTemplateService recruitmentEmailTemplateService;
	private final RecruitmentNotificationEmailSendService recruitmentNotificationEmailSendService;

	@Operation(summary = "모집 알림 메일 내용 및 구독자 수 조회", description = "모집 알림 메일의 내용과 알림 신청 구독자 수를 함께 조회합니다.")
	@GetMapping
	public ApiResponse<RecruitmentEmailTemplateResponse> getRecruitmentNotificationEmail(
			@Parameter(description = "기수 ID (미입력시 현재 모집 중인 기수)") @RequestParam(name = "generationId", required = false) Long generationId) {
		RecruitmentEmailTemplateResponse response = recruitmentEmailTemplateService
				.getRecruitmentEmailTemplate(generationId);
		return ApiResponse.success(response);
	}

	@Operation(summary = "모집 알림 메일 내용 저장", description = "모집 알림 메일의 내용을 저장합니다. 존재하면 수정, 없으면 생성됩니다. 이미 전송된 메일은 수정할 수 없습니다.")
	@PostMapping
	public ApiResponse<Void> saveRecruitmentNotificationEmail(
			@Valid @RequestBody RecruitmentNotificationEmailRequest request) {
		recruitmentEmailTemplateService.saveRecruitmentEmailContent(
				request.generationId(), request.content());
		return ApiResponse.success();
	}

	@Operation(summary = "모집 알림 메일 전송", description = "모집 알림 신청자들에게 메일을 비동기로 전송합니다. 즉시 jobId를 반환하고 백그라운드에서 메일을 발송합니다. 모집이 활성화되어 있어야만"
			+ " 전송할 수 있습니다.")
	@PostMapping("/send")
	public ApiResponse<RecruitmentEmailSendResponse> sendRecruitmentNotificationEmail(
			@Valid @RequestBody RecruitmentNotificationEmailSendRequest request) {
		RecruitmentEmailSendResponse response = recruitmentNotificationEmailSendService
				.sendRecruitmentNotificationEmails(
						request.generationId());
		return ApiResponse.success(response);
	}
}
