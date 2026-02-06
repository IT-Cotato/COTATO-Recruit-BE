package org.cotato.backend.recruit.integratedTest.submit_application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.cotato.backend.recruit.auth.dto.CustomUserDetails;
import org.cotato.backend.recruit.auth.jwt.JwtTokenProvider;
import org.cotato.backend.recruit.domain.application.entity.Application;
import org.cotato.backend.recruit.domain.application.enums.ApplicationPartType;
import org.cotato.backend.recruit.domain.application.enums.PassStatus;
import org.cotato.backend.recruit.domain.application.repository.ApplicationRepository;
import org.cotato.backend.recruit.domain.generation.entity.Generation;
import org.cotato.backend.recruit.domain.generation.repository.GenerationRepository;
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
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@ExtendWith(TestReportManager.class)
@ApiMetadata("POST /api/applications/{applicationId}/submit")
@TestMethodOrder(MethodOrderer.DisplayName.class)
class SubmitApplicationApiTest extends IntegrationTestSupport {

	@Autowired private MockMvc mockMvc;

	@Autowired private UserRepository userRepository;
	@Autowired private GenerationRepository generationRepository;
	@Autowired private ApplicationRepository applicationRepository;
	@Autowired private RecruitmentInformationRepository recruitmentInformationRepository;

	@MockitoBean private JwtTokenProvider jwtTokenProvider;

	@Test
	@DisplayName("01. 지원서를 찾지 못하면 예외처리해야한다")
	@WithMockCustomUser
	void submitApplication_AppNotFound() throws Exception {
		// given
		var auth = setupMemberAndSyncAuth();

		// when & then
		performAndLog(
						mockMvc.perform(
								post("/api/applications/{applicationId}/submit", 9999L)
										.with(
												SecurityMockMvcRequestPostProcessors.authentication(
														auth))
										.with(SecurityMockMvcRequestPostProcessors.csrf())
										.contentType(MediaType.APPLICATION_JSON)))
				.andDo(print())
				.andExpect(status().isNotFound())
				.andExpect(
						jsonPath("$.code")
								.value(PresentationErrorCode.APPLICATION_NOT_FOUND.getCode()));
	}

	@Test
	@DisplayName("02. 사용자 본인의 지원서가 아니면 예외처리해야한다")
	@WithMockCustomUser
	void submitApplication_Forbidden() throws Exception {
		// given
		var auth = setupMemberAndSyncAuth();
		User otherUser =
				userRepository.save(User.createGoogleUser("other@gmail.com", "other", "123"));
		Generation gen = createGeneration();
		createRecruitmentPeriod(gen);
		Application app = Application.createNew(otherUser, gen);
		applicationRepository.save(app);

		// when & then
		performAndLog(
						mockMvc.perform(
								post("/api/applications/{applicationId}/submit", app.getId())
										.with(
												SecurityMockMvcRequestPostProcessors.authentication(
														auth))
										.with(SecurityMockMvcRequestPostProcessors.csrf())
										.contentType(MediaType.APPLICATION_JSON)))
				.andDo(print())
				.andExpect(status().isForbidden())
				.andExpect(
						jsonPath("$.code")
								.value(PresentationErrorCode.APPLICATION_FORBIDDEN.getCode()));
	}

	@Test
	@DisplayName("03. 현재 < 지원시작일이면 예외처리해야한다")
	@WithMockCustomUser
	void submitApplication_PeriodNotStarted() throws Exception {
		// given
		var auth = setupMemberAndSyncAuth();
		User user =
				userRepository
						.findById(((CustomUserDetails) auth.getPrincipal()).getUserId())
						.orElseThrow();
		Generation gen = createGeneration();
		// Set period in future
		createRecruitmentPeriod(
				gen, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(10));

		Application app = Application.createNew(user, gen);
		applicationRepository.save(app);

		// when & then
		performAndLog(
						mockMvc.perform(
								post("/api/applications/{applicationId}/submit", app.getId())
										.with(
												SecurityMockMvcRequestPostProcessors.authentication(
														auth))
										.with(SecurityMockMvcRequestPostProcessors.csrf())
										.contentType(MediaType.APPLICATION_JSON)))
				.andDo(print())
				.andExpect(status().isBadRequest())
				.andExpect(
						jsonPath("$.code")
								.value(
										PresentationErrorCode.RECRUITMENT_PERIOD_NOT_STARTED
												.getCode()));
	}

	@Test
	@DisplayName("04. 현재 > 지원종료일이면 예외처리해야한다")
	@WithMockCustomUser
	void submitApplication_PeriodEnded() throws Exception {
		// given
		var auth = setupMemberAndSyncAuth();
		User user =
				userRepository
						.findById(((CustomUserDetails) auth.getPrincipal()).getUserId())
						.orElseThrow();
		Generation gen = createGeneration();
		// Set period in past
		createRecruitmentPeriod(
				gen, LocalDateTime.now().minusDays(10), LocalDateTime.now().minusDays(1));

		Application app = Application.createNew(user, gen);
		applicationRepository.save(app);

		// when & then
		performAndLog(
						mockMvc.perform(
								post("/api/applications/{applicationId}/submit", app.getId())
										.with(
												SecurityMockMvcRequestPostProcessors.authentication(
														auth))
										.with(SecurityMockMvcRequestPostProcessors.csrf())
										.contentType(MediaType.APPLICATION_JSON)))
				.andDo(print())
				.andExpect(status().isBadRequest())
				.andExpect(
						jsonPath("$.code")
								.value(PresentationErrorCode.RECRUITMENT_PERIOD_ENDED.getCode()));
	}

	@Test
	@DisplayName("05. 해당 기수에 지원서에 이미 제출했으면 예외처리해야한다")
	@WithMockCustomUser
	void submitApplication_AlreadySubmitted() throws Exception {
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
				LocalDate.of(2000, 1, 1),
				"010-0000-0000",
				"Univ",
				"Major",
				1,
				false,
				true,
				ApplicationPartType.BE);
		app.submit(List.of()); // Submit first
		applicationRepository.save(app);

		// when & then
		performAndLog(
						mockMvc.perform(
								post("/api/applications/{applicationId}/submit", app.getId())
										.with(
												SecurityMockMvcRequestPostProcessors.authentication(
														auth))
										.with(SecurityMockMvcRequestPostProcessors.csrf())
										.contentType(MediaType.APPLICATION_JSON)))
				.andDo(print())
				.andExpect(status().isBadRequest())
				.andExpect(
						jsonPath("$.code")
								.value(PresentationErrorCode.ALREADY_SUBMITTED.getCode()));
	}

	@Test
	@DisplayName("06. 지원 파트가 선택되지 않으면 예외처리해야한다")
	@WithMockCustomUser
	void submitApplication_PartTypeNotSelected() throws Exception {
		// given
		var auth = setupMemberAndSyncAuth();
		User user =
				userRepository
						.findById(((CustomUserDetails) auth.getPrincipal()).getUserId())
						.orElseThrow();
		Generation gen = createGeneration();
		createRecruitmentPeriod(gen);

		// applicationPartType을 설정하지 않음 (null 상태)
		Application app = Application.createNew(user, gen);
		applicationRepository.save(app);

		// when & then
		performAndLog(
						mockMvc.perform(
								post("/api/applications/{applicationId}/submit", app.getId())
										.with(
												SecurityMockMvcRequestPostProcessors.authentication(
														auth))
										.with(SecurityMockMvcRequestPostProcessors.csrf())
										.contentType(MediaType.APPLICATION_JSON)))
				.andDo(print())
				.andExpect(status().isBadRequest())
				.andExpect(
						jsonPath("$.code")
								.value(PresentationErrorCode.PART_TYPE_NOT_SELECTED.getCode()));
	}

	@Test
	@DisplayName("07. 필수 필드가 누락되면 예외처리해야한다")
	@WithMockCustomUser
	void submitApplication_RequiredFieldMissing() throws Exception {
		// given
		var auth = setupMemberAndSyncAuth();
		User user =
				userRepository
						.findById(((CustomUserDetails) auth.getPrincipal()).getUserId())
						.orElseThrow();
		Generation gen = createGeneration();
		createRecruitmentPeriod(gen);

		Application app = Application.createNew(user, gen);
		// phoneNumber는 설정하지 않지만, applicationPartType은 설정
		app.updateBasicInfo(
				"test",
				"MALE",
				LocalDate.of(2000, 1, 1),
				null, // phoneNumber를 null로 설정
				"Univ",
				"Major",
				1,
				false,
				true,
				ApplicationPartType.BE);
		applicationRepository.save(app);

		// when & then
		performAndLog(
						mockMvc.perform(
								post("/api/applications/{applicationId}/submit", app.getId())
										.with(
												SecurityMockMvcRequestPostProcessors.authentication(
														auth))
										.with(SecurityMockMvcRequestPostProcessors.csrf())
										.contentType(MediaType.APPLICATION_JSON)))
				.andDo(print())
				.andExpect(status().isBadRequest())
				.andExpect(
						jsonPath("$.code")
								.value(PresentationErrorCode.REQUIRED_FIELD_MISSING.getCode()));
	}

	@Test
	@DisplayName("08. 지원서 제출시 PassStatus.PENDING, isSubmitted=True가 되야한다")
	@WithMockCustomUser
	void submitApplication_Success() throws Exception {
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
				LocalDate.of(2000, 1, 1),
				"010-0000-0000",
				"Univ",
				"Major",
				1,
				false,
				true,
				ApplicationPartType.BE);
		applicationRepository.save(app);

		// when & then
		performAndLog(
						mockMvc.perform(
								post("/api/applications/{applicationId}/submit", app.getId())
										.with(
												SecurityMockMvcRequestPostProcessors.authentication(
														auth))
										.with(SecurityMockMvcRequestPostProcessors.csrf())
										.contentType(MediaType.APPLICATION_JSON)))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value("SUCCESS"));

		// Verify
		Application updated = applicationRepository.findById(app.getId()).orElseThrow();
		assertTrue(updated.getIsSubmitted());
		assertEquals(updated.getPassStatus(), PassStatus.PENDING);
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
		Generation newGeneration =
				Generation.builder()
						.id(1L)
						.isRecruitingActive(true)
						.isAdditionalRecruitmentActive(false)
						.build();
		return generationRepository.saveAndFlush(newGeneration);
	}

	private void createRecruitmentPeriod(Generation gen) {
		createRecruitmentPeriod(
				gen, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
	}

	private void createRecruitmentPeriod(Generation gen, LocalDateTime start, LocalDateTime end) {
		recruitmentInformationRepository.save(
				RecruitmentInformation.builder()
						.generation(gen)
						.informationType(InformationType.RECRUITMENT_START)
						.eventDatetime(start)
						.build());
		recruitmentInformationRepository.save(
				RecruitmentInformation.builder()
						.generation(gen)
						.informationType(InformationType.RECRUITMENT_END)
						.eventDatetime(end)
						.build());
	}
}
