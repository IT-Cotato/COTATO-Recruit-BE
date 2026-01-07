package org.cotato.backend.recruit.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.cotato.backend.recruit.auth.dto.CustomUserDetails;
import org.cotato.backend.recruit.common.file.service.FileService;
import org.cotato.backend.recruit.common.response.ApiResponse;
import org.cotato.backend.recruit.presentation.dto.response.GetPreSignedUrlResponse;
import org.cotato.backend.recruit.presentation.dto.response.PostPreSignedUrlResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "파일 업로드 API", description = "파일 업로드 및 다운로드 관련 API")
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

	private final FileService fileService;

	@Operation(
			summary = "파일 업로드",
			description =
					"PDF 파일을 S3에 업로드합니다. 업로드된 파일의 키를 반환하며, "
							+ "이 값을 지원서 답변 저장 시 fileKey 필드에 포함시켜 전송하면 됩니다.")
	@GetMapping("/posturl")
	public ApiResponse<PostPreSignedUrlResponse> uploadFile(
			@Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
			@Parameter(description = "업로드할 PDF 파일", required = true) @RequestParam("fileName")
					String fileName) {

		PostPreSignedUrlResponse postPreSignedUrlResponse =
				fileService.uploadFile(userDetails.getUserId(), fileName);
		return ApiResponse.success(postPreSignedUrlResponse);
	}

	@Operation(
			summary = "파일 조회 URL 조회",
			description =
					"파일 키로 Pre-signed URL을 생성하여 반환합니다. "
							+ "반환된 URL은 1시간 동안 유효하며, 해당 시간 내에 파일을 조회할 수 있습니다.")
	@GetMapping("/geturl")
	public ApiResponse<GetPreSignedUrlResponse> getPdfUrl(
			@Parameter(description = "S3 파일 키 (예: applications/abc-123.pdf)", required = true)
					@RequestParam("fileKey")
					String fileKey) {

		GetPreSignedUrlResponse response = fileService.generatePreSignedUrl(fileKey);
		return ApiResponse.success(response);
	}
}
