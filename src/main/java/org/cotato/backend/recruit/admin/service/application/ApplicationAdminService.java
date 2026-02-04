package org.cotato.backend.recruit.admin.service.application;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cotato.backend.recruit.admin.error.AdminErrorCode;
import org.cotato.backend.recruit.admin.exception.AdminException;
import org.cotato.backend.recruit.domain.application.entity.Application;
import org.cotato.backend.recruit.domain.application.enums.PassStatus;
import org.cotato.backend.recruit.domain.application.repository.ApplicationRepository;
import org.cotato.backend.recruit.domain.generation.entity.Generation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApplicationAdminService {

	private final ApplicationRepository applicationRepository;

	// 지원서 단건 조회
	public Application getApplication(Long applicationId) {
		return applicationRepository
				.findById(applicationId)
				.orElseThrow(() -> new AdminException(AdminErrorCode.APPLICATION_NOT_FOUND));
	}

	// 합격 상태별 통계 조회
	// 결과 예시
	// [
	// [PassStatus.PASS, BE, 10],
	// [PassStatus.PASS, FE, 20],
	// [PassStatus.WAITLISTED, BE, 30],
	// [PassStatus.WAITLISTED, FE, 40],
	// [PassStatus.FAIL, BE, 50],
	// [PassStatus.FAIL, FE, 60]
	// ]
	public List<Object[]> getPassStatusCounts(Long generationId) {
		return applicationRepository.countByGenerationIdGroupByPassStatusAndApplicationPartType(
				generationId);
	}

	// 특정 기수/상태 지원자 수
	public long countByGenerationAndPassStatus(Generation generation, PassStatus passStatus) {
		return applicationRepository.countByGenerationAndPassStatus(generation, passStatus);
	}

	// 특정 기수/상태 지원자 목록
	public List<Application> findByGenerationAndPassStatus(
			Generation generation, PassStatus passStatus) {
		return applicationRepository.findByGenerationAndPassStatus(generation, passStatus);
	}

	public Application findById(Long applicationId) {
		return applicationRepository
				.findById(applicationId)
				.orElseThrow(() -> new AdminException(AdminErrorCode.APPLICATION_NOT_FOUND));
	}

	public boolean isApplicationExistInThisGeneration(Long generationId) {
		return applicationRepository.existsByGenerationId(generationId);
	}
}
