package org.cotato.backend.recruit.admin.service.applicationAdmin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cotato.backend.recruit.admin.error.ApplicationAdminErrorCode;
import org.cotato.backend.recruit.admin.exception.ApplicationAdminException;
import org.cotato.backend.recruit.domain.application.entity.Application;
import org.cotato.backend.recruit.domain.application.repository.ApplicationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApplicationAdminService {
	private final ApplicationRepository applicationRepository;

	public Application getApplication(Long applicationId) {
		return applicationRepository
				.findById(applicationId)
				.orElseThrow(
						() ->
								new ApplicationAdminException(
										ApplicationAdminErrorCode.APPLICATION_NOT_FOUND));
	}
}
