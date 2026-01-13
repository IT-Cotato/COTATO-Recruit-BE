package org.cotato.backend.recruit.admin.service.staffEvaluation;

import lombok.RequiredArgsConstructor;
import org.cotato.backend.recruit.admin.dto.request.staffEvaluation.CreateStaffEvaluationRequest;
import org.cotato.backend.recruit.admin.dto.response.staffEvaluation.StaffEvaluationResponse;
import org.cotato.backend.recruit.admin.service.application.ApplicationAdminService;
import org.cotato.backend.recruit.domain.application.entity.Application;
import org.cotato.backend.recruit.domain.evaluation.entity.Evaluation;
import org.cotato.backend.recruit.domain.evaluation.enums.EvaluatorType;
import org.cotato.backend.recruit.domain.evaluation.repository.EvaluationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StaffEvaluationService {

	private final EvaluationRepository evaluationRepository;
	private final ApplicationAdminService applicationAdminService;

	public StaffEvaluationResponse getEvaluation(Long applicationId, EvaluatorType evaluatorType) {
		Application application = applicationAdminService.getApplication(applicationId);

		String comment =
				evaluationRepository
						.findByApplicationAndEvaluatorType(application, evaluatorType)
						.map(Evaluation::getComment)
						.orElse(null);

		return StaffEvaluationResponse.from(comment);
	}

	@Transactional
	public void createEvaluation(Long applicationId, CreateStaffEvaluationRequest request) {
		Application application = getApplication(applicationId);
		upsertEvaluation(application, request.evaluatorType(), request.comment());
	}

	private Application getApplication(Long applicationId) {
		return applicationAdminService.getApplication(applicationId);
	}

	private void upsertEvaluation(
			Application application, EvaluatorType evaluatorType, String comment) {
		Evaluation evaluation =
				evaluationRepository
						.findByApplicationAndEvaluatorType(application, evaluatorType)
						.orElseGet(() -> newEvaluation(application, evaluatorType));

		evaluation.updateComment(comment); // 새로 만든 경우에도 값 세팅
		evaluationRepository.save(
				evaluation); // 신규면 insert, 기존이면 merge(또는 JPA dirty checking으로 update)
	}

	private Evaluation newEvaluation(Application application, EvaluatorType evaluatorType) {
		return Evaluation.builder()
				.application(application)
				.evaluatorType(evaluatorType)
				.comment(null)
				.build();
	}
}
