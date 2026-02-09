package org.cotato.backend.recruit.presentation.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.cotato.backend.recruit.common.error.ErrorCode;
import org.cotato.backend.recruit.common.exception.GlobalException;
import org.cotato.backend.recruit.common.util.DateFormatter;
import org.cotato.backend.recruit.domain.application.dto.ApplicationEtcData;
import org.cotato.backend.recruit.domain.application.entity.Application;
import org.cotato.backend.recruit.domain.application.entity.ApplicationAnswer;
import org.cotato.backend.recruit.domain.application.entity.ApplicationEtcInfo;
import org.cotato.backend.recruit.domain.application.repository.ApplicationAnswerRepository;
import org.cotato.backend.recruit.domain.application.repository.ApplicationEtcInfoRepository;
import org.cotato.backend.recruit.domain.application.repository.ApplicationRepository;
import org.cotato.backend.recruit.domain.question.entity.Question;
import org.cotato.backend.recruit.domain.question.enums.QuestionType;
import org.cotato.backend.recruit.domain.user.entity.User;
import org.cotato.backend.recruit.domain.user.repository.UserRepository;
import org.cotato.backend.recruit.presentation.dto.response.AnswerResponse;
import org.cotato.backend.recruit.presentation.dto.response.BasicInfoResponse;
import org.cotato.backend.recruit.presentation.dto.response.EtcAnswerResponse;
import org.cotato.backend.recruit.presentation.dto.response.MyPageApplicationResponse;
import org.cotato.backend.recruit.presentation.dto.response.PartQuestionResponse;
import org.cotato.backend.recruit.presentation.dto.response.RecruitmentScheduleResponse;
import org.cotato.backend.recruit.presentation.error.PresentationErrorCode;
import org.cotato.backend.recruit.presentation.exception.PresentationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 제출된 지원서 조회 서비스 지원자가 제출한 지원서를 조회하는 기능을 담당합니다. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubmittedApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationAnswerRepository applicationAnswerRepository;
    private final ApplicationEtcInfoRepository applicationEtcInfoRepository;
    private final QuestionService questionService;
    private final RecruitmentService recruitmentService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    /**
     * 사용자의 지원서 목록 조회 (마이페이지)
     *
     * @param userId 사용자 ID
     * @return 마이페이지 지원서 목록 응답
     */
    public List<MyPageApplicationResponse> getMyApplications(Long userId) {
        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));

        return applicationRepository.findByUser(user).stream()
                .map(MyPageApplicationResponse::of)
                .toList();
    }

    /**
     * 제출된 지원서의 기본 인적사항 조회
     *
     * @param userId        사용자 ID
     * @param applicationId 지원서 ID
     * @return 기본 인적사항 응답
     */
    public BasicInfoResponse getBasicInfo(Long userId, Long applicationId) {
        Application application = getApplicationWithAuth(applicationId, userId);
        return BasicInfoResponse.from(application);
    }

    /**
     * 제출된 지원서의 파트별 질문 및 답변 조회
     *
     * @param userId        사용자 ID
     * @param applicationId 지원서 ID
     * @return 파트별 질문 및 답변 목록
     */
    public PartQuestionResponse getPartQuestionsWithAnswers(Long userId, Long applicationId) {
        Application application = getApplicationWithAuth(applicationId, userId);

        // 선택한 파트 검증
        if (application.getApplicationPartType() == null) {
            throw new PresentationException(PresentationErrorCode.PART_TYPE_NOT_SELECTED);
        }

        // ApplicationPartType을 QuestionType으로 변환
        QuestionType questionType = application.getApplicationPartType().toQuestionType();

        // 선택한 파트 질문 조회
        List<Question> partQuestions = questionService.getQuestionsByGenerationAndQuestionType(
                application.getGeneration(), questionType);

        // 저장된 답변 조회 및 매핑
        List<ApplicationAnswer> savedAnswers = applicationAnswerRepository.findByApplication(application);

        List<PartQuestionResponse.QuestionWithAnswerResponse> questionList = mapQuestionsWithAnswers(partQuestions,
                savedAnswers);

        return PartQuestionResponse.of(
                questionList, application.getPdfFileUrl(), application.getPdfFileKey());
    }

    /**
     * 제출된 지원서의 기타 정보 조회
     *
     * @param userId        사용자 ID
     * @param applicationId 지원서 ID
     * @return 기타 정보 응답
     */
    public EtcAnswerResponse getEtcInfo(Long userId, Long applicationId) {
        Application application = getApplicationWithAuth(applicationId, userId);

        // 모집 일정 조회
        RecruitmentScheduleResponse schedule = recruitmentService.getRecruitmentSchedule(application.getGeneration());

        // ApplicationEtcInfo에서 JSON 데이터 조회
        ApplicationEtcData etcData = getEtcData(application);

        // 날짜 포맷팅
        String interviewStartDate = DateFormatter.formatMonthDay(schedule.interviewStartDate());
        String interviewEndDate = DateFormatter.formatMonthDay(schedule.interviewEndDate());
        String otDate = DateFormatter.formatMonthDay(schedule.otDate());

        return EtcAnswerResponse.of(etcData, interviewStartDate, interviewEndDate, otDate);
    }

    /**
     * 지원서 조회 및 권한 검증
     *
     * @param applicationId 지원서 ID
     * @param userId        사용자 ID
     * @return 지원서 엔티티
     */
    private Application getApplicationWithAuth(Long applicationId, Long userId) {
        Application application = applicationRepository
                .findById(applicationId)
                .orElseThrow(
                        () -> new PresentationException(
                                PresentationErrorCode.APPLICATION_NOT_FOUND));

        // 사용자 권한 검증
        application.validateUser(userId);

        return application;
    }

    /**
     * 질문 목록을 저장된 답변과 함께 매핑
     *
     * @param questions    질문 목록
     * @param savedAnswers 저장된 답변 목록
     * @return 질문과 답변이 매핑된 응답 목록
     */
    private List<PartQuestionResponse.QuestionWithAnswerResponse> mapQuestionsWithAnswers(
            List<Question> questions, List<ApplicationAnswer> savedAnswers) {
        // 질문 ID를 키로 하는 답변 맵 생성
        Map<Long, ApplicationAnswer> answerMap = savedAnswers.stream()
                .collect(
                        Collectors.toMap(
                                answer -> answer.getQuestion().getId(), answer -> answer));

        // 질문과 저장된 답변을 함께 반환
        return questions.stream()
                .map(
                        q -> {
                            ApplicationAnswer savedAnswer = answerMap.get(q.getId());
                            AnswerResponse answerResponse = savedAnswer != null ? AnswerResponse.from(savedAnswer)
                                    : null;

                            return PartQuestionResponse.QuestionWithAnswerResponse.of(
                                    q, answerResponse);
                        })
                .collect(Collectors.toList());
    }

    /**
     * ApplicationEtcInfo에서 JSON 데이터를 ApplicationEtcData로 변환
     *
     * @param application 지원서
     * @return ApplicationEtcData (없으면 모든 필드 null인 객체 반환)
     */
    private ApplicationEtcData getEtcData(Application application) {
        Optional<ApplicationEtcInfo> etcInfoOpt = applicationEtcInfoRepository.findByApplication(application);

        if (etcInfoOpt.isEmpty() || etcInfoOpt.get().getEtcData() == null) {
            // 기타 정보가 없으면 모든 필드 null인 객체 반환
            return new ApplicationEtcData(null, null, null, null, null, null);
        }

        try {
            return objectMapper.readValue(etcInfoOpt.get().getEtcData(), ApplicationEtcData.class);
        } catch (JsonProcessingException e) {
            throw new PresentationException(PresentationErrorCode.INVALID_JSON_FORMAT);
        }
    }
}
