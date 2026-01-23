package org.cotato.backend.recruit.integratedTest.get_questions_with_answers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import org.cotato.backend.recruit.auth.dto.CustomUserDetails;
import org.cotato.backend.recruit.auth.jwt.JwtTokenProvider;
import org.cotato.backend.recruit.domain.application.entity.Application;
import org.cotato.backend.recruit.domain.application.entity.ApplicationAnswer;
import org.cotato.backend.recruit.domain.application.enums.ApplicationPartType;
import org.cotato.backend.recruit.domain.application.repository.ApplicationAnswerRepository;
import org.cotato.backend.recruit.domain.application.repository.ApplicationRepository;
import org.cotato.backend.recruit.domain.generation.entity.Generation;
import org.cotato.backend.recruit.domain.generation.repository.GenerationRepository;
import org.cotato.backend.recruit.domain.question.entity.Question;
import org.cotato.backend.recruit.domain.question.enums.QuestionType;
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
@ApiMetadata("GET /api/applications/{applicationId}/part-questions")
@TestMethodOrder(MethodOrderer.DisplayName.class)
class GetQuestionsWithAnswersApiTest extends IntegrationTestSupport {

	@Autowired private MockMvc mockMvc;

	@Autowired private UserRepository userRepository;
	@Autowired private GenerationRepository generationRepository;
	@Autowired private ApplicationRepository applicationRepository;
	@Autowired private QuestionRepository questionRepository;
	@Autowired private ApplicationAnswerRepository applicationAnswerRepository;
	@Autowired private RecruitmentInformationRepository recruitmentInformationRepository;

	@MockitoBean private JwtTokenProvider jwtTokenProvider;

	@Test
	@DisplayName("01. 지원서에 선택한 파트가 없으면 예외처리해야한다.")
	@WithMockCustomUser
	void getQuestions_PartNotSelected() throws Exception {
		// given
		var auth = setupMemberAndSyncAuth();
		User user =
				userRepository
						.findById(((CustomUserDetails) auth.getPrincipal()).getUserId())
						.orElseThrow();
		Generation gen = createGeneration();
		createRecruitmentPeriod(gen);

		Application app = Application.createNew(user, gen);
		if (app.getApplicationPartType() != null) {
			throw new IllegalArgumentException("이 테스트는 Part가 null이어야 수행 가능합니다");
		}

		applicationRepository.save(app);

		// when & then
		performAndLog( // performAndLog 적용
						mockMvc.perform(
								get("/api/applications/{applicationId}/part-questions", app.getId())
										.with(
												SecurityMockMvcRequestPostProcessors.authentication(
														auth))))
				.andDo(print())
				.andExpect(status().isBadRequest())
				.andExpect(
						jsonPath("$.code")
								.value(PresentationErrorCode.PART_TYPE_NOT_SELECTED.getCode()));
	}

	@Test
	@DisplayName("02. 사용자 본인의 지원서가 아니면 예외처리해야한다")
	@WithMockCustomUser
	void getQuestions_Forbidden() throws Exception {
		// given
		var auth = setupMemberAndSyncAuth(); // User 1

		// Create another user
		User otherUser =
				userRepository.save(User.createGoogleUser("other@gmail.com", "other", "123"));
		Generation gen = createGeneration();
		createRecruitmentPeriod(gen);

		Application app = Application.createNew(otherUser, gen);
		app.updateBasicInfo(
				"test",
				"MALE",
				java.time.LocalDate.now(),
				"010-0000-0000",
				"Univ",
				"Major",
				1,
				false,
				true,
				ApplicationPartType.BE);
		applicationRepository.save(app);

		// when & then
		performAndLog( // performAndLog 적용
						mockMvc.perform(
								get("/api/applications/{applicationId}/part-questions", app.getId())
										.with(
												org.springframework.security.test.web.servlet
														.request
														.SecurityMockMvcRequestPostProcessors
														.authentication(auth))))
				.andDo(print())
				.andExpect(status().isForbidden())
				.andExpect(
						jsonPath("$.code")
								.value(PresentationErrorCode.APPLICATION_FORBIDDEN.getCode()));
	}

	@Test
	@DisplayName("03. 지원서를 찾지 못하면 예외처리해야한다")
	@WithMockCustomUser
	void getQuestions_NotFound() throws Exception {
		// given
		var auth = setupMemberAndSyncAuth();

		// when & then
		performAndLog( // performAndLog 적용
						mockMvc.perform(
								get("/api/applications/{applicationId}/part-questions", 99999L)
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
	@DisplayName("04. 지원서 조회에 성공하면 PartQuestionResponse를 반환해야한다")
	@WithMockCustomUser
	void getQuestions_Success() throws Exception {
		// given
		var auth = setupMemberAndSyncAuth();
		User user =
				userRepository
						.findById(((CustomUserDetails) auth.getPrincipal()).getUserId())
						.orElseThrow();
		Generation gen = createGeneration();
		createRecruitmentPeriod(gen);

		Application app = Application.createNew(user, gen);
		app.updateBasicInfo(
				"test",
				"MALE",
				java.time.LocalDate.now(),
				"010-0000-0000",
				"Univ",
				"Major",
				1,
				false,
				true,
				ApplicationPartType.BE);
		applicationRepository.saveAndFlush(app);

		Question q1 =
				questionRepository.save(
						Question.builder()
								.generation(gen)
								.sequence(1)
								.content("Question 1")
								.questionType(QuestionType.BE)
								.maxByte(500)
								.build());

		ApplicationAnswer ans1 = ApplicationAnswer.of(app, q1, "Answer 1");
		applicationAnswerRepository.save(ans1);

		// when & then
		performAndLog( // performAndLog 적용
						mockMvc.perform(
								get("/api/applications/{applicationId}/part-questions", app.getId())
										.with(
												SecurityMockMvcRequestPostProcessors.authentication(
														auth))))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value("SUCCESS"))
				.andExpect(jsonPath("$.message").value("요청이 성공적으로 처리되었습니다.")) // 메시지 검증 추가
				.andExpect(jsonPath("$.data.questionsWithAnswers[0].questionId").value(q1.getId()))
				.andExpect(jsonPath("$.data.questionsWithAnswers[0].content").value("Question 1"));
	}

	private UsernamePasswordAuthenticationToken setupMemberAndSyncAuth() {
		User user =
				userRepository.save(User.createGoogleUser("test@gmail.com", "testUser", "123456"));
		CustomUserDetails userDetails =
				new CustomUserDetails(user.getId(), user.getEmail(), User.Role.APPLICANT);
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

	private void createRecruitmentPeriod(Generation gen) {
		recruitmentInformationRepository.save(
				RecruitmentInformation.builder()
						.generation(gen)
						.informationType(InformationType.RECRUITMENT_START)
						.eventDatetime(LocalDateTime.now().minusDays(1))
						.build());
		recruitmentInformationRepository.save(
				RecruitmentInformation.builder()
						.generation(gen)
						.informationType(InformationType.RECRUITMENT_END)
						.eventDatetime(LocalDateTime.now().plusDays(1))
						.build());
	}
}
