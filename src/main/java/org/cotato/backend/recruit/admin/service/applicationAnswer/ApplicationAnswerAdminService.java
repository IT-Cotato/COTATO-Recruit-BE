package org.cotato.backend.recruit.admin.service.applicationAnswer;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.cotato.backend.recruit.domain.application.entity.Application;
import org.cotato.backend.recruit.domain.application.entity.ApplicationAnswer;
import org.cotato.backend.recruit.domain.application.repository.ApplicationAnswerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApplicationAnswerAdminService {

	private final ApplicationAnswerRepository applicationAnswerRepository;

	public List<ApplicationAnswer> getAnswers(Application application) {
		return applicationAnswerRepository.findByApplication(application);
	}
}
