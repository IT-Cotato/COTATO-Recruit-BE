package org.cotato.backend.recruit.admin.service.applicationView;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.cotato.backend.recruit.admin.dto.response.applicationView.AdminApplicationBasicInfoResponse;
import org.cotato.backend.recruit.admin.dto.response.applicationView.AdminApplicationEtcQuestionsResponse;
import org.cotato.backend.recruit.admin.dto.response.applicationView.AdminApplicationPartQuestionResponse;
import org.cotato.backend.recruit.admin.service.application.ApplicationAdminService;
import org.cotato.backend.recruit.admin.service.applicationAnswer.ApplicationAnswerAdminService;
import org.cotato.backend.recruit.admin.service.applicationEtcInfo.ApplicationEtcInfoAdminService;
import org.cotato.backend.recruit.admin.service.question.QuestionAdminService;
import org.cotato.backend.recruit.admin.service.recruitmentInformation.RecruitmentInformationAdminService;
import org.cotato.backend.recruit.domain.application.dto.ApplicationEtcData;
import org.cotato.backend.recruit.domain.application.entity.Application;
import org.cotato.backend.recruit.domain.application.entity.ApplicationAnswer;
import org.cotato.backend.recruit.domain.generation.entity.Generation;
import org.cotato.backend.recruit.domain.question.entity.Question;
import org.cotato.backend.recruit.domain.question.enums.QuestionType;
import org.cotato.backend.recruit.domain.recruitmentInformation.entity.RecruitmentInformation;
import org.cotato.backend.recruit.domain.recruitmentInformation.enums.InformationType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ApplicationViewService {

	private final ApplicationAnswerAdminService applicationAnswerAdminService;
	private final ApplicationAdminService applicationAdminService;
	private final QuestionAdminService questionAdminService;
	private final RecruitmentInformationAdminService recruitmentInformationAdminService;
	private final ApplicationEtcInfoAdminService applicationEtcInfoAdminService;

	public AdminApplicationBasicInfoResponse getBasicInfo(Long applicationId) {
		Application application = applicationAdminService.getApplication(applicationId);
		return AdminApplicationBasicInfoResponse.from(application);
	}

	public AdminApplicationPartQuestionResponse getPartQuestionsWithAnswers(
			Long applicationId, QuestionType questionType) {
		Application application = applicationAdminService.getApplication(applicationId);

		List<AdminApplicationPartQuestionResponse.AdminPartQuestionResponse> questionList =
				getQuestionsWithAnswers(application, questionType);

		return AdminApplicationPartQuestionResponse.of(
				questionList, application.getPdfFileUrl(), application.getPdfFileKey());
	}

	public AdminApplicationEtcQuestionsResponse getEtcQuestionsWithAnswers(Long applicationId) {
		Application application = applicationAdminService.getApplication(applicationId);

		// ApplicationEtcInfo에서 JSON 데이터 조회
		ApplicationEtcData etcData = applicationEtcInfoAdminService.getEtcData(application);

		// 모집 일정 조회
		RecruitmentInformation interviewStart =
				getRecruitmentInfo(application.getGeneration(), InformationType.INTERVIEW_START);
		RecruitmentInformation interviewEnd =
				getRecruitmentInfo(application.getGeneration(), InformationType.INTERVIEW_END);
		RecruitmentInformation ot =
				getRecruitmentInfo(application.getGeneration(), InformationType.OT);

		return AdminApplicationEtcQuestionsResponse.of(
				etcData,
				interviewStart.getEventDatetime(),
				interviewEnd.getEventDatetime(),
				ot.getEventDatetime());
	}

	private RecruitmentInformation getRecruitmentInfo(Generation generation, InformationType type) {
		return recruitmentInformationAdminService.getRecruitmentInformation(generation, type);
	}

	private List<AdminApplicationPartQuestionResponse.AdminPartQuestionResponse>
			getQuestionsWithAnswers(Application application, QuestionType questionType) {
		List<Question> questions =
				questionAdminService.getQuestionsByGenerationAndQuestionType(
						application.getGeneration(), questionType);

		List<ApplicationAnswer> answers = applicationAnswerAdminService.getAnswers(application);
		Map<Long, ApplicationAnswer> answerMap = createAnswerMap(answers);

		return questions.stream()
				.map(question -> createResponse(question, answerMap.get(question.getId())))
				.toList();
	}

	private AdminApplicationPartQuestionResponse.AdminPartQuestionResponse createResponse(
			Question question, ApplicationAnswer answer) {
		return AdminApplicationPartQuestionResponse.AdminPartQuestionResponse.from(
				question, answer);
	}

	private Map<Long, ApplicationAnswer> createAnswerMap(List<ApplicationAnswer> answers) {
		return answers.stream()
				.collect(
						Collectors.toMap(
								a -> a.getQuestion().getId(), // Key: 질문 ID
								Function.identity() // Value: 답변 객체
								));
	}
}
