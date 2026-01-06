package org.cotato.backend.recruit.common.file.service;

import org.cotato.backend.recruit.presentation.dto.response.GetPreSignedUrlResponse;
import org.cotato.backend.recruit.presentation.dto.response.PostPreSignedUrlResponse;

/** 파일 업로드 및 다운로드 서비스 인터페이스 */
public interface FileService {

	/**
	 * 파일 업로드를 위한 Pre-signed URL 생성
	 *
	 * @param userId 사용자 ID
	 * @param fileName 업로드할 파일 이름
	 * @return Pre-signed URL 및 파일 키 정보
	 */
	PostPreSignedUrlResponse uploadFile(Long userId, String fileName);

	/**
	 * 파일 조회를 위한 Pre-signed URL 생성
	 *
	 * @param fileKey S3에 저장된 파일의 키
	 * @return Pre-signed URL 응답
	 */
	GetPreSignedUrlResponse generatePreSignedUrl(String fileKey);
}
