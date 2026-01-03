package org.cotato.backend.recruit.admin.service.staffEvaluation;

import lombok.RequiredArgsConstructor;
import org.cotato.backend.recruit.admin.dto.request.staffEvaluation.CreateStaffEvaluationRequest;
import org.cotato.backend.recruit.admin.dto.response.staffEvaluation.StaffEvaluationResponse;
import org.cotato.backend.recruit.admin.service.applicationAdmin.ApplicationAdminService;
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
		Evaluation evaluation =
				evaluationRepository
						.findByApplicationIdAndEvaluatorType(application.getId(), evaluatorType)
						.orElse(null);

		return StaffEvaluationResponse.from(evaluation != null ? evaluation.getComment() : null);
	}

	@Transactional
	public void createEvaluation(Long applicationId, CreateStaffEvaluationRequest request) {
		Application application = applicationAdminService.getApplication(applicationId);

		Evaluation evaluation =
				evaluationRepository
						.findByApplicationIdAndEvaluatorType(
								application.getId(), request.evaluatorType())
						.orElse(null);

		if (evaluation != null) {
			update(evaluation, request);
		} else {
			insert(application, request);
		}
	}

	private void update(Evaluation evaluation, CreateStaffEvaluationRequest request) {
		evaluation.updateComment(request.comment());
	}

	private void insert(Application application, CreateStaffEvaluationRequest request) {
		evaluationRepository.save(
				Evaluation.builder()
						.application(application)
						.evaluatorType(request.evaluatorType())
						.comment(request.comment())
						.build());
	}
}
