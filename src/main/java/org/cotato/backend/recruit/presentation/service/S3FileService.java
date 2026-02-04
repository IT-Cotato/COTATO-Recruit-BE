package org.cotato.backend.recruit.presentation.service;

import java.time.Duration;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cotato.backend.recruit.common.error.ErrorCode;
import org.cotato.backend.recruit.common.exception.GlobalException;
import org.cotato.backend.recruit.common.file.service.FileService;
import org.cotato.backend.recruit.presentation.dto.response.GetPreSignedUrlResponse;
import org.cotato.backend.recruit.presentation.dto.response.PostPreSignedUrlResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

/**
 * AWS S3를 이용한 파일 서비스 구현체
 *
 * <p>Pre-signed URL을 생성하여 클라이언트가 S3에 직접 파일을 업로드/다운로드할 수 있도록 합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class S3FileService implements FileService {

	private final S3Presigner s3Presigner;

	@Value("${cloud.aws.s3.bucket}")
	private String bucketName;

	@Value("${file.pre-signed-url.expiration:3600}") // 기본값: 1시간 (3600초)
	private long preSignedUrlExpiration;

	private static final String UPLOAD_DIRECTORY = "applications";

	@Override
	public PostPreSignedUrlResponse uploadFile(Long userId, String fileName) {
		try {
			validateFileName(fileName);
			String key = generateFileKey(userId, fileName);
			String preSignedUrl = createPutPreSignedUrl(key);

			return new PostPreSignedUrlResponse(preSignedUrl, key);

		} catch (GlobalException e) {
			throw e;
		} catch (Exception e) {
			log.error(
					"업로드용 Pre-signed URL 생성 실패 - UserId: {}, FileName: {}, Error: {}",
					userId,
					fileName,
					e.getMessage());
			throw new GlobalException(ErrorCode.PRE_SIGNED_URL_GENERATION_FAILED);
		}
	}

	@Override
	public GetPreSignedUrlResponse generatePreSignedUrl(String fileKey) {
		try {
			validateFileKey(fileKey);
			String preSignedUrl = createGetPreSignedUrl(fileKey);
			return new GetPreSignedUrlResponse(preSignedUrl);

		} catch (GlobalException e) {
			throw e;
		} catch (Exception e) {
			log.error("다운로드용 Pre-signed URL 생성 실패 - Key: {}, Error: {}", fileKey, e.getMessage());
			throw new GlobalException(ErrorCode.PRE_SIGNED_URL_GENERATION_FAILED);
		}
	}

	/** 파일 키 생성 */
	private String generateFileKey(Long userId, String fileName) {
		return UPLOAD_DIRECTORY + "/" + userId + "/" + UUID.randomUUID() + "/" + fileName;
	}

	/** PUT용 Pre-signed URL 생성 */
	private String createPutPreSignedUrl(String key) {
		PutObjectRequest putObjectRequest =
				PutObjectRequest.builder()
						.bucket(bucketName)
						.key(key)
						.contentType("application/pdf")
						.build();

		PutObjectPresignRequest preSignRequest =
				PutObjectPresignRequest.builder()
						.signatureDuration(Duration.ofSeconds(preSignedUrlExpiration))
						.putObjectRequest(putObjectRequest)
						.build();

		PresignedPutObjectRequest preSignedRequest = s3Presigner.presignPutObject(preSignRequest);

		return preSignedRequest.url().toString();
	}

	/** GET용 Pre-signed URL 생성 */
	private String createGetPreSignedUrl(String key) {
		GetObjectRequest getObjectRequest =
				GetObjectRequest.builder()
						.bucket(bucketName)
						.key(key)
						.responseContentType("application/pdf")
						.build();

		GetObjectPresignRequest preSignRequest =
				GetObjectPresignRequest.builder()
						.signatureDuration(Duration.ofSeconds(preSignedUrlExpiration))
						.getObjectRequest(getObjectRequest)
						.build();

		PresignedGetObjectRequest preSignedRequest = s3Presigner.presignGetObject(preSignRequest);

		return preSignedRequest.url().toString();
	}

	/** 파일명 검증 */
	private void validateFileName(String fileName) {
		if (fileName == null || fileName.isBlank()) {
			throw new GlobalException(ErrorCode.INVALID_FILE_TYPE, "파일명이 비어있습니다.");
		}

		// Path Traversal 공격 방지
		if (fileName.contains("..")) {
			throw new GlobalException(ErrorCode.INVALID_FILE_TYPE, "유효하지 않은 파일명입니다.");
		}

		// PDF 확장자 검증
		if (!fileName.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
			throw new GlobalException(ErrorCode.INVALID_FILE_TYPE, "PDF 파일만 업로드 가능합니다.");
		}
	}

	/** 파일 키 검증 */
	private void validateFileKey(String fileKey) {
		// Path Traversal 공격 방지
		if (fileKey.contains("..")) {
			throw new GlobalException(ErrorCode.INVALID_FILE_TYPE, "유효하지 않은 파일 경로입니다.");
		}

		// applications/ 디렉토리 내의 파일인지 확인
		if (!fileKey.startsWith(UPLOAD_DIRECTORY + "/")) {
			throw new GlobalException(ErrorCode.INVALID_FILE_TYPE, "유효하지 않은 파일 경로입니다.");
		}
	}
}
