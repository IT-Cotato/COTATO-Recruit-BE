package org.cotato.backend.recruit.integratedTest.save_answers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.cotato.backend.recruit.auth.dto.CustomUserDetails;
import org.cotato.backend.recruit.auth.jwt.JwtTokenProvider;
import org.cotato.backend.recruit.domain.application.entity.Application;
import org.cotato.backend.recruit.domain.application.entity.ApplicationAnswer;
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
import org.cotato.backend.recruit.presentation.dto.request.PartAnswerRequest;
import org.cotato.backend.recruit.presentation.dto.request.PartAnswerRequest.AnswerRequest;
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
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@ExtendWith(TestReportManager.class)
@ApiMetadata("POST /api/applications/{applicationId}/answers")
@TestMethodOrder(MethodOrderer.DisplayName.class)
class SaveAnswersApiTest extends IntegrationTestSupport {

	@Autowired private MockMvc mockMvc;
	@Autowired private ObjectMapper objectMapper;

	@Autowired private UserRepository userRepository;
	@Autowired private GenerationRepository generationRepository;
	@Autowired private ApplicationRepository applicationRepository;
	@Autowired private QuestionRepository questionRepository;
	@Autowired private ApplicationAnswerRepository applicationAnswerRepository;
	@Autowired private RecruitmentInformationRepository recruitmentInformationRepository;

	@MockitoBean private JwtTokenProvider jwtTokenProvider;

	@Test
	@DisplayName("01. 지원서를 찾지 못하면 예외처리해야한다")
	@WithMockCustomUser
	void saveAnswers_AppNotFound() throws Exception {
		// given
		var auth = setupMemberAndSyncAuth();
		AnswerRequest answerRequest = new AnswerRequest(1L, "Answer");
		PartAnswerRequest request = new PartAnswerRequest(List.of(answerRequest), "url", "key");

		// when & then
		performAndLog(
				mockMvc.perform(
								post("/api/applications/{applicationId}/answers", 9999L)
										.with(
												SecurityMockMvcRequestPostProcessors.authentication(
														auth))
										.with(SecurityMockMvcRequestPostProcessors.csrf())
										.contentType(MediaType.APPLICATION_JSON)
										.content(objectMapper.writeValueAsString(request)))
						.andDo(print())
						.andExpect(status().isNotFound())
						.andExpect(
								jsonPath("$.code")
										.value(
												PresentationErrorCode.APPLICATION_NOT_FOUND
														.getCode())));
	}

	@Test
	@DisplayName("02. 사용자 본인의 지원서가 아니면 예외처리해야한다")
	@WithMockCustomUser
	void saveAnswers_Forbidden() throws Exception {
		// given
		var auth = setupMemberAndSyncAuth();
		User otherUser =
				userRepository.saveAndFlush(
						User.createGoogleUser("other@gmail.com", "other", "123"));
		Generation gen = createGeneration();
		createRecruitmentPeriod(gen);
		Application app = Application.createNew(otherUser, gen);
		applicationRepository.saveAndFlush(app);

		AnswerRequest answerRequest = new AnswerRequest(1L, "Answer");
		PartAnswerRequest request = new PartAnswerRequest(List.of(answerRequest), "url", "key");

		// when & then
		performAndLog(
				mockMvc.perform(
								post("/api/applications/{applicationId}/answers", app.getId())
										.with(
												SecurityMockMvcRequestPostProcessors.authentication(
														auth))
										.with(SecurityMockMvcRequestPostProcessors.csrf())
										.contentType(MediaType.APPLICATION_JSON)
										.content(objectMapper.writeValueAsString(request)))
						.andDo(print())
						.andExpect(status().isForbidden())
						.andExpect(
								jsonPath("$.code")
										.value(
												PresentationErrorCode.APPLICATION_FORBIDDEN
														.getCode())));
	}

	@Test
	@DisplayName("03. 기존 답변이 있으면 업데이트해야한다")
	@WithMockCustomUser
	void saveAnswers_UpdateExisting() throws Exception {
		// given
		var auth = setupMemberAndSyncAuth();
		User user =
				userRepository
						.findById(((CustomUserDetails) auth.getPrincipal()).getUserId())
						.orElseThrow();
		Generation gen = createGeneration();
		createRecruitmentPeriod(gen);
		Application app = Application.createNew(user, gen);
		applicationRepository.saveAndFlush(app);

		Question q1 =
				questionRepository.saveAndFlush(
						Question.builder()
								.generation(gen)
								.sequence(1)
								.content("Q1")
								.questionType(QuestionType.BE)
								.maxByte(500)
								.build());

		ApplicationAnswer ans1 = ApplicationAnswer.of(app, q1, "Old Answer");
		applicationAnswerRepository.saveAndFlush(ans1);

		AnswerRequest answerRequest = new AnswerRequest(q1.getId(), "New Answer");
		PartAnswerRequest request =
				new PartAnswerRequest(List.of(answerRequest), "new-url", "new-key");

		// when & then
		performAndLog(
				mockMvc.perform(
								post("/api/applications/{applicationId}/answers", app.getId())
										.with(
												SecurityMockMvcRequestPostProcessors.authentication(
														auth))
										.with(SecurityMockMvcRequestPostProcessors.csrf())
										.contentType(MediaType.APPLICATION_JSON)
										.content(objectMapper.writeValueAsString(request)))
						.andDo(print())
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.code").value("SUCCESS")));

		// 1. 답변이 하나만 유지되고 있는지 확인 (리스트 크기가 1이어야 함)
		List<ApplicationAnswer> answers = applicationAnswerRepository.findAll();
		assertEquals(1, answers.size(), "답변 개수는 1개여야 합니다.");

		// 2. 답변 내용이 "New Answer"로 변경되었는지 확인
		ApplicationAnswer updatedAnswer =
				applicationAnswerRepository.findById(ans1.getId()).orElseThrow();
		assertEquals("New Answer", updatedAnswer.getContent(), "답변 내용이 업데이트되지 않았습니다.");

		// 3. 지원서의 PDF URL 등이 업데이트되었는지 확인
		Application updatedApp = applicationRepository.findById(app.getId()).orElseThrow();
		assertEquals("new-url", updatedApp.getPdfFileUrl());
		assertEquals("new-key", updatedApp.getPdfFileKey());
	}

	@Test
	@DisplayName("04. 기존 답변이 없으면 새 답변을 생성해야한다")
	@WithMockCustomUser
	void saveAnswers_CreateNew() throws Exception {
		// given
		var auth = setupMemberAndSyncAuth();
		User user =
				userRepository
						.findById(((CustomUserDetails) auth.getPrincipal()).getUserId())
						.orElseThrow();
		Generation gen = createGeneration();
		createRecruitmentPeriod(gen);
		Application app = Application.createNew(user, gen);
		applicationRepository.saveAndFlush(app);

		Question q1 =
				questionRepository.saveAndFlush(
						Question.builder()
								.generation(gen)
								.sequence(1)
								.content("Q1")
								.questionType(QuestionType.BE)
								.maxByte(500) // Assumed default
								.build());

		AnswerRequest answerRequest = new AnswerRequest(q1.getId(), "New Answer");
		PartAnswerRequest request = new PartAnswerRequest(List.of(answerRequest), "url", "key");

		// when & then
		performAndLog(
				mockMvc.perform(
								post("/api/applications/{applicationId}/answers", app.getId())
										.with(
												SecurityMockMvcRequestPostProcessors.authentication(
														auth))
										.with(SecurityMockMvcRequestPostProcessors.csrf())
										.contentType(MediaType.APPLICATION_JSON)
										.content(objectMapper.writeValueAsString(request)))
						.andDo(print())
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.code").value("SUCCESS")));

		// Verify creation
		List<ApplicationAnswer> answers = applicationAnswerRepository.findAll();
		assertEquals(1, answers.size());
	}

	private UsernamePasswordAuthenticationToken setupMemberAndSyncAuth() {
		User user =
				userRepository.saveAndFlush(
						User.createGoogleUser("test@gmail.com", "testUser", "123456"));
		CustomUserDetails userDetails =
				new CustomUserDetails(user.getId(), user.getEmail(), User.Role.APPLICANT);
		return new UsernamePasswordAuthenticationToken(
				userDetails, null, userDetails.getAuthorities());
	}

	private Generation createGeneration() {
		return generationRepository.saveAndFlush(
				Generation.builder()
						.id(1L)
						.isRecruitingActive(true)
						.isAdditionalRecruitmentActive(false)
						.build());
	}

	private void createRecruitmentPeriod(Generation gen) {
		recruitmentInformationRepository.saveAndFlush(
				RecruitmentInformation.builder()
						.generation(gen)
						.informationType(InformationType.RECRUITMENT_START)
						.eventDatetime(LocalDateTime.now().minusDays(1))
						.build());
		recruitmentInformationRepository.saveAndFlush(
				RecruitmentInformation.builder()
						.generation(gen)
						.informationType(InformationType.RECRUITMENT_END)
						.eventDatetime(LocalDateTime.now().plusDays(1))
						.build());
	}
}
