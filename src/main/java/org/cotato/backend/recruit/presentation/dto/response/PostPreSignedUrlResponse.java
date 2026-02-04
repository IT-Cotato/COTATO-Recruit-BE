package org.cotato.backend.recruit.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Pre-signed URL 응답 (업로드용)")
public record PostPreSignedUrlResponse(
		@Schema(
						description = "S3 업로드용 Pre-signed URL (PUT 요청으로 파일 업로드)",
						example =
								"https://bucket.s3.ap-northeast-2.amazonaws.com/applications/abc-123.pdf?X-Amz-Algorithm=...")
				String preSignedUrl,
		@Schema(
						description = "S3에 저장될 파일 키 (지원서 답변에 저장할 값), 임시 저장 시, fileKey 필드에 포함시켜 전송",
						example = "applications/abc-123-def.pdf")
				String key) {}
