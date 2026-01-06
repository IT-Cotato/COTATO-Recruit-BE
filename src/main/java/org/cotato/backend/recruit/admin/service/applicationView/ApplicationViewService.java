package org.cotato.backend.recruit.admin.service.applicationView;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.cotato.backend.recruit.admin.dto.response.applicationView.AdminApplicationBasicInfoResponse;
import org.cotato.backend.recruit.admin.dto.response.applicationView.AdminApplicationPartQuestionResponse;
import org.cotato.backend.recruit.admin.service.application.ApplicationAdminService;
import org.cotato.backend.recruit.admin.service.applicationAnswer.ApplicationAnswerAdminService;
import org.cotato.backend.recruit.admin.service.question.QuestionAdminService;
import org.cotato.backend.recruit.common.util.ByteManager;
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

	public List<AdminApplicationPartQuestionResponse> getPartQuestionsWithAnswers(Long applicationId, PartType part) {
		return getQuestionsWithAnswers(applicationId, part);
	}

	public List<AdminApplicationPartQuestionResponse> getEtcQuestionsWithAnswers(Long applicationId) {
		return getQuestionsWithAnswers(applicationId, PartType.ETC);
	}

	private List<AdminApplicationPartQuestionResponse> getQuestionsWithAnswers(Long applicationId, PartType partType) {
		Application application = applicationAdminService.getApplication(applicationId);

		List<Question> questions = questionAdminService.getQuestionsByGenerationAndPartType(application.getGeneration(),
				partType);

		List<ApplicationAnswer> answers = applicationAnswerAdminService.getAnswers(application);
		Map<Long, ApplicationAnswer> answerMap = createAnswerMap(answers);

		return questions.stream()
				.map(question -> createResponse(question, answerMap.get(question.getId())))
				.toList();
	}

	/**
	 * ✅ 충돌 기능 통합 포인트
	 * - from(question, answer) 방식이 있더라도,
	 * - HEAD에서 추가된 필드(fileKey/fileUrl/byteSize/isChecked 등) 포함하려면 builder 방식이 안전함.
	 */
	private AdminApplicationPartQuestionResponse createResponse(Question question, ApplicationAnswer answer) {
		String content = (answer != null ? answer.getContent() : null);

		return AdminApplicationPartQuestionResponse.builder()
				.sequence(question.getSequence())
				.questionContent(question.getContent())
				// 답변이 있으면 답변 타입, 없으면 질문 타입
				.answerType(answer != null ? answer.getAnswerType() : question.getAnswerType())
				// 체크 여부는 답변이 없으면 null 유지(요구사항에 따라 false로 바꿔도 됨)
				.isChecked(answer != null ? answer.getIsChecked() : null)
				.content(content)
				.fileKey(answer != null ? answer.getFileKey() : null)
				.fileUrl(answer != null ? answer.getFileUrl() : null)
				.byteSize(content != null ? ByteManager.getByteSize(content) : 0)
				.build();
	}

	private Map<Long, ApplicationAnswer> createAnswerMap(List<ApplicationAnswer> answers) {
		return answers.stream()
				.collect(Collectors.toMap(
						a -> a.getQuestion().getId(), // Key: 질문 ID
						Function.identity() // Value: 답변 객체
				));
	}
}
