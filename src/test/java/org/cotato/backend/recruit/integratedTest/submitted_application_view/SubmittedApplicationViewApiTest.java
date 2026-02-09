package org.cotato.backend.recruit.integratedTest.submitted_application_view;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.cotato.backend.recruit.auth.dto.CustomUserDetails;
import org.cotato.backend.recruit.auth.jwt.JwtTokenProvider;
import org.cotato.backend.recruit.domain.application.dto.ApplicationEtcData;
import org.cotato.backend.recruit.domain.application.entity.Application;
import org.cotato.backend.recruit.domain.application.entity.ApplicationEtcInfo;
import org.cotato.backend.recruit.domain.application.enums.ApplicationPartType;
import org.cotato.backend.recruit.domain.application.enums.DiscoveryPath;
import org.cotato.backend.recruit.domain.application.repository.ApplicationAnswerRepository;
import org.cotato.backend.recruit.domain.application.repository.ApplicationEtcInfoRepository;
import org.cotato.backend.recruit.domain.application.repository.ApplicationRepository;
import org.cotato.backend.recruit.domain.generation.entity.Generation;
import org.cotato.backend.recruit.domain.generation.repository.GenerationRepository;
import org.cotato.backend.recruit.domain.question.repository.QuestionRepository;
import org.cotato.backend.recruit.domain.recruitmentInformation.entity.RecruitmentInformation;
import org.cotato.backend.recruit.domain.recruitmentInformation.enums.InformationType;
import org.cotato.backend.recruit.domain.recruitmentInformation.repository.RecruitmentInformationRepository;
import org.cotato.backend.recruit.domain.user.entity.User;
import org.cotato.backend.recruit.domain.user.repository.UserRepository;
import org.cotato.backend.recruit.excelReport.TestReportManager;
import org.cotato.backend.recruit.presentation.error.PresentationErrorCode;
import org.cotato.backend.recruit.testsupport.ApiMetadata;
import org.cotato.backend.recruit.testsupport.IntegrationTestSupport;
import org.cotato.backend.recruit.testsupport.WithMockCustomUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@ExtendWith(TestReportManager.class)
@ApiMetadata("GET /api/submitted-applications/{applicationId}/*")
@TestMethodOrder(MethodOrderer.DisplayName.class)
class SubmittedApplicationViewApiTest extends IntegrationTestSupport {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private GenerationRepository generationRepository;
	@Autowired
	private ApplicationRepository applicationRepository;
	@Autowired
	private QuestionRepository questionRepository;
	@Autowired
	private ApplicationAnswerRepository applicationAnswerRepository;
	@Autowired
	private ApplicationEtcInfoRepository applicationEtcInfoRepository;
	@Autowired
	private RecruitmentInformationRepository recruitmentInformationRepository;
	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private JwtTokenProvider jwtTokenProvider;

	// --------------------------------------------------------
	// GET /api/submitted-applications/{applicationId}/basic-info
	// --------------------------------------------------------

	@Test
	@DisplayName("01. 기본정보: 지원서를 찾지 못하면 예외처리해야한다")
	@WithMockCustomUser
	void getBasicInfo_NotFound() throws Exception {
		// given
		var auth = setupMemberAndSyncAuth();

		// when & then
		performAndLog(
				mockMvc.perform(
						get(
								"/api/submitted-applications/{applicationId}/basic-info",
								99999L)
								.with(
										SecurityMockMvcRequestPostProcessors.authentication(
												auth))))
				.andDo(print())
				.andExpect(status().isNotFound())
				.andExpect(
						jsonPath("$.code")
								.value(PresentationErrorCode.APPLICATION_NOT_FOUND.getCode()));
	}

	@Test
	@DisplayName("02. 기본정보: 본인의 지원서가 아니면 예외처리해야한다")
	@WithMockCustomUser
	void getBasicInfo_Forbidden() throws Exception {
		// given
		var auth = setupMemberAndSyncAuth();
		User otherUser = userRepository.save(User.createGoogleUser("other@gmail.com", "other", "123"));
		Generation gen = createGeneration();

		Application app = createSubmittedApplication(otherUser, gen);

		// when & then
		performAndLog(
				mockMvc.perform(
						get(
								"/api/submitted-applications/{applicationId}/basic-info",
								app.getId())
								.with(
										SecurityMockMvcRequestPostProcessors.authentication(
												auth))))
				.andDo(print())
				.andExpect(status().isForbidden())
				.andExpect(
						jsonPath("$.code")
								.value(PresentationErrorCode.APPLICATION_FORBIDDEN.getCode()));
	}

	@Test
	@DisplayName("03. 기본정보: 제출된 지원서 조회에 성공하면 BasicInfoResponse를 반환해야한다")
	@WithMockCustomUser
	void getBasicInfo_Success() throws Exception {
		// given
		var auth = setupMemberAndSyncAuth();
		User user = userRepository
				.findById(((CustomUserDetails) auth.getPrincipal()).getUserId())
				.orElseThrow();
		Generation gen = createGeneration();

		Application app = createSubmittedApplication(user, gen);

		// when & then
		performAndLog(
				mockMvc.perform(
						get(
								"/api/submitted-applications/{applicationId}/basic-info",
								app.getId())
								.with(
										SecurityMockMvcRequestPostProcessors.authentication(
												auth))))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value("SUCCESS"))
				.andExpect(jsonPath("$.data.applicationId").value(app.getId()))
				.andExpect(jsonPath("$.data.name").value("홍길동"))
				.andExpect(jsonPath("$.data.gender").value("남"))
				.andExpect(jsonPath("$.data.phoneNumber").value("010-1234-5678"))
				.andExpect(jsonPath("$.data.university").value("코타토대학교"))
				.andExpect(jsonPath("$.data.major").value("컴퓨터공학"))
				.andExpect(jsonPath("$.data.completedSemesters").value(4))
				.andExpect(jsonPath("$.data.applicationPartType").value("BE"));
	}

	// --------------------------------------------------------
	// GET /api/submitted-applications/{applicationId}/part-questions
	// --------------------------------------------------------

	@Test
	@DisplayName("04. 파트질문: 지원서를 찾지 못하면 예외처리해야한다")
	@WithMockCustomUser
	void getPartQuestions_NotFound() throws Exception {
		// given
		var auth = setupMemberAndSyncAuth();

		// when & then
		performAndLog(
				mockMvc.perform(
						get(
								"/api/submitted-applications/{applicationId}/part-questions",
								99999L)
								.with(
										SecurityMockMvcRequestPostProcessors.authentication(
												auth))))
				.andDo(print())
				.andExpect(status().isNotFound())
				.andExpect(
						jsonPath("$.code")
								.value(PresentationErrorCode.APPLICATION_NOT_FOUND.getCode()));
	}

	@Test
	@DisplayName("05. 파트질문: 본인의 지원서가 아니면 예외처리해야한다")
	@WithMockCustomUser
	void getPartQuestions_Forbidden() throws Exception {
		// given
		var auth = setupMemberAndSyncAuth();
		User otherUser = userRepository.save(User.createGoogleUser("other@gmail.com", "other", "123"));
		Generation gen = createGeneration();

		Application app = createSubmittedApplication(otherUser, gen);

		// when & then
		performAndLog(
				mockMvc.perform(
						get(
								"/api/submitted-applications/{applicationId}/part-questions",
								app.getId())
								.with(
										SecurityMockMvcRequestPostProcessors.authentication(
												auth))))
				.andDo(print())
				.andExpect(status().isForbidden())
				.andExpect(
						jsonPath("$.code")
								.value(PresentationErrorCode.APPLICATION_FORBIDDEN.getCode()));
	}

	@Test
	@DisplayName("06. 파트질문: 파트가 선택되지 않았으면 예외처리해야한다")
	@WithMockCustomUser
	void getPartQuestions_PartNotSelected() throws Exception {
		// given
		var auth = setupMemberAndSyncAuth();
		User user = userRepository
				.findById(((CustomUserDetails) auth.getPrincipal()).getUserId())
				.orElseThrow();
		Generation gen = createGeneration();

		Application app = Application.createNew(user, gen);
		applicationRepository.save(app);

		// when & then
		performAndLog(
				mockMvc.perform(
						get(
								"/api/submitted-applications/{applicationId}/part-questions",
								app.getId())
								.with(
										SecurityMockMvcRequestPostProcessors.authentication(
												auth))))
				.andDo(print())
				.andExpect(status().isBadRequest())
				.andExpect(
						jsonPath("$.code")
								.value(PresentationErrorCode.PART_TYPE_NOT_SELECTED.getCode()));
	}

	// @Test
	// @DisplayName("07. 파트질문: 제출된 지원서 조회에 성공하면 PartQuestionResponse를 반환해야한다")
	// @WithMockCustomUser
	// void getPartQuestions_Success() throws Exception {
	// // given
	// var auth = setupMemberAndSyncAuth();
	// User user =
	// userRepository
	// .findById(((CustomUserDetails) auth.getPrincipal()).getUserId())
	// .orElseThrow();
	// Generation gen = createGeneration();

	// Application app = createSubmittedApplication(user, gen);

	// Question q1 =
	// questionRepository.save(
	// Question.builder()
	// .generation(gen)
	// .sequence(1)
	// .content("백엔드 질문 1")
	// .questionType(QuestionType.BE)
	// .maxLength(500)
	// .build());

	// ApplicationAnswer ans1 = ApplicationAnswer.of(app, q1, "백엔드 답변 1");
	// applicationAnswerRepository.save(ans1);

	// // when & then
	// performAndLog(
	// mockMvc.perform(
	// get(
	// "/api/submitted-applications/{applicationId}/part-questions",
	// app.getId())
	// .with(
	// SecurityMockMvcRequestPostProcessors.authentication(
	// auth))))
	// .andDo(print())
	// .andExpect(status().isOk())
	// .andExpect(jsonPath("$.code").value("SUCCESS"))
	// .andExpect(jsonPath("$.data.questionsWithAnswers[0].questionId").value(q1.getId()))
	// .andExpect(jsonPath("$.data.questionsWithAnswers[0].content").value("백엔드 질문
	// 1"))
	// .andExpect(
	// jsonPath("$.data.questionsWithAnswers[0].savedAnswer.content")
	// .value("백엔드 답변 1"));
	// }

	// --------------------------------------------------------
	// GET /api/submitted-applications/{applicationId}/etc-info
	// --------------------------------------------------------

	@Test
	@DisplayName("08. 기타정보: 지원서를 찾지 못하면 예외처리해야한다")
	@WithMockCustomUser
	void getEtcInfo_NotFound() throws Exception {
		// given
		var auth = setupMemberAndSyncAuth();

		// when & then
		performAndLog(
				mockMvc.perform(
						get("/api/submitted-applications/{applicationId}/etc-info", 99999L)
								.with(
										SecurityMockMvcRequestPostProcessors.authentication(
												auth))))
				.andDo(print())
				.andExpect(status().isNotFound())
				.andExpect(
						jsonPath("$.code")
								.value(PresentationErrorCode.APPLICATION_NOT_FOUND.getCode()));
	}

	@Test
	@DisplayName("09. 기타정보: 본인의 지원서가 아니면 예외처리해야한다")
	@WithMockCustomUser
	void getEtcInfo_Forbidden() throws Exception {
		// given
		var auth = setupMemberAndSyncAuth();
		User otherUser = userRepository.save(User.createGoogleUser("other@gmail.com", "other", "123"));
		Generation gen = createGeneration();
		createRecruitmentSchedule(gen);

		Application app = createSubmittedApplication(otherUser, gen);

		// when & then
		performAndLog(
				mockMvc.perform(
						get(
								"/api/submitted-applications/{applicationId}/etc-info",
								app.getId())
								.with(
										SecurityMockMvcRequestPostProcessors.authentication(
												auth))))
				.andDo(print())
				.andExpect(status().isForbidden())
				.andExpect(
						jsonPath("$.code")
								.value(PresentationErrorCode.APPLICATION_FORBIDDEN.getCode()));
	}

	@Test
	@DisplayName("10. 기타정보: 제출된 지원서 조회에 성공하면 EtcAnswerResponse를 반환해야한다")
	@WithMockCustomUser
	void getEtcInfo_Success() throws Exception {
		// given
		var auth = setupMemberAndSyncAuth();
		User user = userRepository
				.findById(((CustomUserDetails) auth.getPrincipal()).getUserId())
				.orElseThrow();
		Generation gen = createGeneration();
		createRecruitmentSchedule(gen);

		Application app = createSubmittedApplication(user, gen);

		// 기타 정보 저장
		ApplicationEtcData etcData = ApplicationEtcData.of(
				DiscoveryPath.INSTAGRAM, "병행활동 내용", "면접 불가 시간", true, true, true);
		String jsonData = objectMapper.writeValueAsString(etcData);
		ApplicationEtcInfo etcInfo = ApplicationEtcInfo.createNew(app);
		etcInfo.updateEtcData(jsonData);
		applicationEtcInfoRepository.save(etcInfo);

		// when & then
		performAndLog(
				mockMvc.perform(
						get(
								"/api/submitted-applications/{applicationId}/etc-info",
								app.getId())
								.with(
										SecurityMockMvcRequestPostProcessors.authentication(
												auth))))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value("SUCCESS"))
				.andExpect(jsonPath("$.data.discoveryPath.selectedAnswer").value("INSTAGRAM"))
				.andExpect(jsonPath("$.data.parallelActivities").value("병행활동 내용"))
				.andExpect(jsonPath("$.data.unavailableInterviewTimes").value("면접 불가 시간"))
				.andExpect(jsonPath("$.data.sessionAttendance").value(true))
				.andExpect(jsonPath("$.data.mandatoryEvents").value(true))
				.andExpect(jsonPath("$.data.privacyPolicy").value(true));
	}

	// --------------------------------------------------------
	// Helper Methods
	// --------------------------------------------------------

	private UsernamePasswordAuthenticationToken setupMemberAndSyncAuth() {
		User user = userRepository.save(User.createGoogleUser("test@gmail.com", "testUser", "123456"));
		CustomUserDetails userDetails = new CustomUserDetails(user.getId(), user.getEmail(), User.Role.APPLICANT);
		return new UsernamePasswordAuthenticationToken(
				userDetails, null, userDetails.getAuthorities());
	}

	private Generation createGeneration() {
		return generationRepository.save(
				Generation.builder()
						.id(1L)
						.isRecruitingActive(true)
						.isAdditionalRecruitmentActive(false)
						.build());
	}

	private Application createSubmittedApplication(User user, Generation gen) {
		Application app = Application.createNew(user, gen);
		app.updateBasicInfo(
				"홍길동",
				"남",
				LocalDate.of(2000, 1, 1),
				"010-1234-5678",
				"코타토대학교",
				"컴퓨터공학",
				4,
				false,
				true,
				ApplicationPartType.BE);
		app.submit(List.of());
		return applicationRepository.save(app);
	}

	private void createRecruitmentSchedule(Generation gen) {
		// 면접 시작일
		recruitmentInformationRepository.save(
				RecruitmentInformation.builder()
						.generation(gen)
						.informationType(InformationType.INTERVIEW_START)
						.eventDatetime(LocalDateTime.of(2026, 3, 3, 0, 0))
						.build());
		// 면접 종료일
		recruitmentInformationRepository.save(
				RecruitmentInformation.builder()
						.generation(gen)
						.informationType(InformationType.INTERVIEW_END)
						.eventDatetime(LocalDateTime.of(2026, 3, 5, 0, 0))
						.build());
		// OT 날짜
		recruitmentInformationRepository.save(
				RecruitmentInformation.builder()
						.generation(gen)
						.informationType(InformationType.OT)
						.eventDatetime(LocalDateTime.of(2026, 3, 6, 0, 0))
						.build());
	}
}
