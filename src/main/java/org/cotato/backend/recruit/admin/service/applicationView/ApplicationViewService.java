package org.cotato.backend.recruit.admin.service.applicationView;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.cotato.backend.recruit.admin.dto.response.applicationView.AdminApplicationBasicInfoResponse;
import org.cotato.backend.recruit.admin.dto.response.applicationView.AdminApplicationPartQuestionResponse;
import org.cotato.backend.recruit.admin.service.application.ApplicationAdminService;
import org.cotato.backend.recruit.admin.service.applicationAnswerAdmin.ApplicationAnswerAdminService;
import org.cotato.backend.recruit.admin.service.questionAdmin.QuestionAdminService;
import org.cotato.backend.recruit.domain.application.entity.Application;
import org.cotato.backend.recruit.domain.application.entity.ApplicationAnswer;
import org.cotato.backend.recruit.domain.question.entity.Question;
import org.cotato.backend.recruit.domain.question.enums.PartType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ApplicationViewService {

	private final ApplicationAnswerAdminService applicationAnswerAdminService;
	private final ApplicationAdminService applicationAdminService;
	private final QuestionAdminService questionAdminService;

	public AdminApplicationBasicInfoResponse getBasicInfo(Long applicationId) {
		Application application = applicationAdminService.getApplication(applicationId);
		return AdminApplicationBasicInfoResponse.from(application);
	}

	public List<AdminApplicationPartQuestionResponse> getPartQuestionsWithAnswers(
			Long applicationId, PartType part) {
		return getQuestionsWithAnswers(applicationId, part);
	}

	public List<AdminApplicationPartQuestionResponse> getEtcQuestionsWithAnswers(
			Long applicationId) {
		return getQuestionsWithAnswers(applicationId, PartType.ETC);
	}

	private List<AdminApplicationPartQuestionResponse> getQuestionsWithAnswers(
			Long applicationId, PartType partType) {
		Application application = applicationAdminService.getApplication(applicationId);

		List<Question> questions =
				questionAdminService.getQuestionsByGenerationAndPartType(
						application.getGeneration(), partType);

		List<ApplicationAnswer> answers = applicationAnswerAdminService.getAnswers(application);

		Map<Long, ApplicationAnswer> answerMap = createAnswerMap(answers);

		return questions.stream()
				.map(question -> createResponse(question, answerMap.get(question.getId())))
				.toList();
	}

	private AdminApplicationPartQuestionResponse createResponse(
			Question question, ApplicationAnswer answer) {
		return AdminApplicationPartQuestionResponse.from(question, answer);
	}

	private Map<Long, ApplicationAnswer> createAnswerMap(List<ApplicationAnswer> answers) {
		return answers.stream()
				.collect(
						Collectors.toMap(
								answer -> answer.getQuestion().getId(), // Key: 질문 ID
								Function.identity() // Value: 답변 객체 그 자체
								));
	}
}
