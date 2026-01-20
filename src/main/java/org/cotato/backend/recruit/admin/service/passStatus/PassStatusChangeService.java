package org.cotato.backend.recruit.admin.service.passStatus;

import lombok.RequiredArgsConstructor;
import org.cotato.backend.recruit.admin.dto.request.applicationView.PassStatusChangeRequest;
import org.cotato.backend.recruit.admin.service.application.ApplicationAdminService;
import org.cotato.backend.recruit.domain.application.entity.Application;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PassStatusChangeService {

	private final ApplicationAdminService applicationAdminService;

	@Transactional
	public void updatePassStatus(Long applicationId, PassStatusChangeRequest request) {
		Application application = applicationAdminService.findById(applicationId);

		application.updatePassStatus(request.passStatus());
	}
}
