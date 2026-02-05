package org.cotato.backend.recruit.integratedTest.start_application;

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
import org.cotato.backend.recruit.domain.application.repository.ApplicationRepository;
import org.cotato.backend.recruit.domain.generation.entity.Generation;
import org.cotato.backend.recruit.domain.generation.repository.GenerationRepository;
import org.cotato.backend.recruit.domain.recruitmentInformation.entity.RecruitmentInformation;
import org.cotato.backend.recruit.domain.recruitmentInformation.enums.InformationType;
import org.cotato.backend.recruit.domain.recruitmentInformation.repository.RecruitmentInformationRepository;
import org.cotato.backend.recruit.domain.user.entity.User;
import org.cotato.backend.recruit.domain.user.repository.UserRepository;
import org.cotato.backend.recruit.excelReport.TestReportManager;
import org.cotato.backend.recruit.testsupport.ApiMetadata;
import org.cotato.backend.recruit.testsupport.IntegrationTestSupport;
import org.cotato.backend.recruit.testsupport.WithMockCustomUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
@ApiMetadata("POST /api/applications/start")
@ExtendWith(TestReportManager.class)
@TestMethodOrder(MethodOrderer.DisplayName.class)
class StartApplicationApiTest extends IntegrationTestSupport {

	@Autowired private MockMvc mockMvc;

	@Autowired private UserRepository userRepository;
	@Autowired private GenerationRepository generationRepository;
	@Autowired private ApplicationRepository applicationRepository;
	@Autowired private RecruitmentInformationRepository recruitmentInformationRepository;

	// 필터 통과를 위해 Mock 처리
	@MockitoBean private JwtTokenProvider jwtTokenProvider;

	@Test
	@DisplayName("01. 현재 활성화 된 기수가 없으면 예외처리해야한다")
	@WithMockCustomUser
	void startApplication_NoGeneration() throws Exception {
		// given
		var auth = setupMemberAndSyncAuth(); // 멤버 생성 및 ID 동기화

		// when & then
		performAndLog(
						mockMvc.perform(
								post("/api/applications/start")
										.contentType(MediaType.APPLICATION_JSON)
										.with(
												SecurityMockMvcRequestPostProcessors.authentication(
														auth))))
				.andDo(print())
				.andExpect(status().isNotFound()) // 404
				.andExpect(jsonPath("$.code").value("RE002"));
	}

	@Test
	@DisplayName("02. 현재 < 지원시작일이면 예외처리해야한다")
	@WithMockCustomUser
	void startApplication_PeriodNotStarted() throws Exception {
		// given
		var auth = setupMemberAndSyncAuth();
		Generation gen = createGeneration(); // 활성화된 기수 생성
		createRecruitmentPeriod(
				gen, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(10));

		// when & then
		performAndLog(
						mockMvc.perform(
								post("/api/applications/start")
										.contentType(MediaType.APPLICATION_JSON)
										.with(
												SecurityMockMvcRequestPostProcessors.authentication(
														auth))))
				.andDo(print())
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("AP007"));
	}

	@Test
	@DisplayName("03. 현재 > 지원종료일이면 예외처리해야한다")
	@WithMockCustomUser
	void startApplication_PeriodEnded() throws Exception {
		// given
		var auth = setupMemberAndSyncAuth();
		Generation gen = createGeneration();
		createRecruitmentPeriod(
				gen, LocalDateTime.now().minusDays(10), LocalDateTime.now().minusDays(1));

		// when & then
		performAndLog(
						mockMvc.perform(
								post("/api/applications/start")
										.contentType(MediaType.APPLICATION_JSON)
										.with(
												SecurityMockMvcRequestPostProcessors.authentication(
														auth))))
				.andDo(print())
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("AP005"));
	}

	@Test
	@DisplayName("04. 이미 제출한 지원서가 있으면 예외처리해야한다")
	@WithMockCustomUser
	void startApplication_AlreadySubmitted() throws Exception {
		// given
		var auth = setupMemberAndSyncAuth();
		User userEntity =
				userRepository
						.findById(((CustomUserDetails) auth.getPrincipal()).getUserId())
						.orElseThrow();

		Generation gen = createGeneration();
		createRecruitmentPeriod(
				gen, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));

		// '제출 완료' 상태의 지원서 저장
		Application app = Application.createNew(userEntity, gen);
		// 필수 정보 입력 후 제출
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
				ApplicationPartType.DE);
		app.submit(List.of());
		applicationRepository.saveAndFlush(app);

		// when & then
		performAndLog(
						mockMvc.perform(
								post("/api/applications/start")
										.contentType(MediaType.APPLICATION_JSON)
										.with(
												SecurityMockMvcRequestPostProcessors.authentication(
														auth))))
				.andDo(print())
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("AP002"));
	}

	@Test
	@DisplayName("05. 기존 지원서가 존재하면 해당 지원서를 반환해야한다")
	@WithMockCustomUser
	void startApplication_ReturnExisting() throws Exception {
		// given
		var auth = setupMemberAndSyncAuth();
		User userEntity =
				userRepository
						.findById(((CustomUserDetails) auth.getPrincipal()).getUserId())
						.orElseThrow();
		Generation gen = createGeneration();
		createRecruitmentPeriod(
				gen, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));

		// '작성 중' 상태의 지원서 저장
		Application existingApp = Application.createNew(userEntity, gen);
		applicationRepository.saveAndFlush(existingApp);

		// when & then
		performAndLog(
						mockMvc.perform(
								post("/api/applications/start")
										.contentType(MediaType.APPLICATION_JSON)
										.with(
												SecurityMockMvcRequestPostProcessors.authentication(
														auth))))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value("SUCCESS"))
				.andExpect(
						jsonPath("$.data.applicationId").value(existingApp.getId())) // 기존 ID와 일치 확인
				.andExpect(jsonPath("$.data.isSubmitted").value(false));
	}

	@Test
	@DisplayName("06. 기존 지원서가 없으면 새로 생성해 반환해야한다")
	@WithMockCustomUser
	void startApplication_CreateNew() throws Exception {
		// given
		var auth = setupMemberAndSyncAuth();
		// 진행 중인 기수 생성, 하지만 지원서는 없음
		Generation gen = createGeneration();
		createRecruitmentPeriod(
				gen, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));

		// when & then
		performAndLog(
						mockMvc.perform(
								post("/api/applications/start")
										.contentType(MediaType.APPLICATION_JSON)
										.with(
												SecurityMockMvcRequestPostProcessors.authentication(
														auth))))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value("SUCCESS"))
				.andExpect(jsonPath("$.data.applicationId").exists()) // ID가 새로 생성되었는지
				.andExpect(jsonPath("$.data.isSubmitted").value(false));
	}

	// --------------------------------------------------------
	// Helper Methods
	// --------------------------------------------------------

	/** 멤버를 DB에 저장하고, 생성된 진짜 ID로 Authentication을 반환합니다. */
	private UsernamePasswordAuthenticationToken setupMemberAndSyncAuth() {
		// 1. 실제 DB 저장 (여기서 ID 자동 생성)
		User user =
				userRepository.saveAndFlush(
						User.createGoogleUser("test@gmail.com", "testUser", "123456"));

		// 2. CustomUserDetails 생성 (진짜 ID 주입)
		CustomUserDetails userDetails =
				new CustomUserDetails(
						user.getId(),
						user.getEmail(), // 혹은 username
						User.Role.APPLICANT);

		// 3. Auth Token 생성
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

	private void createRecruitmentPeriod(Generation gen, LocalDateTime start, LocalDateTime end) {
		recruitmentInformationRepository.saveAndFlush(
				RecruitmentInformation.builder()
						.generation(gen)
						.informationType(InformationType.RECRUITMENT_START)
						.eventDatetime(start)
						.build());
		recruitmentInformationRepository.saveAndFlush(
				RecruitmentInformation.builder()
						.generation(gen)
						.informationType(InformationType.RECRUITMENT_END)
						.eventDatetime(end)
						.build());
	}
}
