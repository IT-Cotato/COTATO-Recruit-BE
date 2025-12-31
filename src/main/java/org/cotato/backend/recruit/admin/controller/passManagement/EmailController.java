package org.cotato.backend.recruit.admin.controller.passManagement;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.cotato.backend.recruit.admin.dto.request.email.EmailContentUpdateRequest;
import org.cotato.backend.recruit.admin.dto.response.email.EmailSendResponse;
import org.cotato.backend.recruit.admin.dto.response.email.EmailTemplateResponse;
import org.cotato.backend.recruit.admin.service.email.EmailSendService;
import org.cotato.backend.recruit.admin.service.email.EmailTemplateService;
import org.cotato.backend.recruit.common.response.ApiResponse;
import org.cotato.backend.recruit.domain.email.enums.TemplateType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "이메일 관리 API", description = "합격/불합격/예비합격 메일 관리 API")
@RestController
@RequestMapping("/api/admin/recruitment-mails")
@RequiredArgsConstructor
public class EmailController {

	private final EmailTemplateService emailTemplateService;
	private final EmailSendService emailSendService;

	@Operation(
			summary = "메일 내용 및 대상자 수 조회",
			description = "지정된 타입(PASS, FAIL, PRELIMINARY)의 메일 템플릿과 대상자 수를 함께 조회합니다.")
	@GetMapping
	public ApiResponse<EmailTemplateResponse> getEmailTemplate(
			@Parameter(description = "템플릿 타입 (PASS, FAIL, PRELIMINARY)", required = true)
					@RequestParam
					TemplateType templateType,
			@Parameter(description = "기수 ID (미입력시 현재 모집 중인 기수)") @RequestParam(required = false)
					Long generationId) {
		EmailTemplateResponse response =
				emailTemplateService.getEmailTemplate(templateType, generationId);
		return ApiResponse.success(response);
	}

	@Operation(
			summary = "메일 내용 저장",
			description = "메일 템플릿의 내용을 저장합니다. 존재하면 수정, 없으면 생성됩니다. 이미 전송된 메일은 수정할 수 없습니다.")
	@PostMapping
	public ApiResponse<Void> saveEmailContent(
			@Parameter(description = "템플릿 타입 (PASS, FAIL, PRELIMINARY)", required = true)
					@RequestParam
					TemplateType templateType,
			@Parameter(description = "기수 ID (미입력시 현재 모집 중인 기수)") @RequestParam(required = false)
					Long generationId,
			@Parameter(description = "메일 내용", required = true) @Valid @RequestBody
					EmailContentUpdateRequest request) {
		emailTemplateService.saveEmailContent(templateType, generationId, request);
		return ApiResponse.success();
	}

	@Operation(summary = "메일 전송", description = "지정된 타입의 메일을 대상자들에게 전송합니다. ")
	@PostMapping("/send")
	public ApiResponse<EmailSendResponse> sendEmails(
			@Parameter(description = "템플릿 타입 (PASS, FAIL, PRELIMINARY)", required = true)
					@RequestParam
					TemplateType templateType,
			@Parameter(description = "기수 ID (미입력시 현재 모집 중인 기수)") @RequestParam(required = false)
					Long generationId) {
		EmailSendResponse response = emailSendService.sendEmails(templateType, generationId);
		return ApiResponse.success(response);
	}
}
