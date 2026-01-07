package org.cotato.backend.recruit.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "파일 조회 응답")
public record GetPreSignedUrlResponse(
		@Schema(
						description = "Pre-signed URL (1시간 동안 유효)",
						example =
								"https://bucket.s3.ap-northeast-2.amazonaws.com/applications/abc-123.pdf?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=...")
				String pdfUrl) {}
