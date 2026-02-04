package org.cotato.backend.recruit.common.email.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cotato.backend.recruit.admin.error.AdminErrorCode;
import org.cotato.backend.recruit.admin.exception.AdminException;
import org.cotato.backend.recruit.domain.email.entity.EmailSendJob;
import org.cotato.backend.recruit.domain.email.repository.EmailSendJobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailSendJobService {

	private final EmailSendJobRepository emailSendJobRepository;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void completeJob(Long jobId, int successCount, int failCount) {
		EmailSendJob job =
				emailSendJobRepository
						.findById(jobId)
						.orElseThrow(() -> new AdminException(AdminErrorCode.EMAIL_JOB_NOT_FOUND));
		job.complete(successCount, failCount);
		emailSendJobRepository.save(job);
		log.info("이메일 작업 완료 처리 - jobId: {}, 성공: {}, 실패: {}", jobId, successCount, failCount);
	}
}
