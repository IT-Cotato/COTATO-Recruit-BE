package org.cotato.backend.recruit.integratedTest.application_status;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
@ApiMetadata("GET /api/applications/status")
@ExtendWith(TestReportManager.class)
@TestMethodOrder(MethodOrderer.DisplayName.class)
class ApplicationStatusApiTest extends IntegrationTestSupport {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private GenerationRepository generationRepository;
	@Autowired
	private ApplicationRepository applicationRepository;
	@Autowired
	private RecruitmentInformationRepository recruitmentInformationRepository;

	// 필터 통과를 위해 Mock 처리
	@MockitoBean
	private JwtTokenProvider jwtTokenProvider;

	@Test
	@DisplayName("01. 활성 기수가 없으면 예외처리해야한다")
	@WithMockCustomUser
	void getApplicationStatus_NoGeneration() throws Exception {
		// given
		var auth = setupMemberAndSyncAuth();

		// when & then
		performAndLog(
				mockMvc.perform(
						get("/api/applications/status")
								.contentType(MediaType.APPLICATION_JSON)
								.with(
										SecurityMockMvcRequestPostProcessors.authentication(
												auth))))
				.andDo(print())
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("GE001"));
	}

	@Test
	@DisplayName("02. 지원서가 없고 모집이 종료된 경우 applicationId는 null, isSubmitted는 false, isEnd는 true를 반환해야한다")
	@WithMockCustomUser
	void getApplicationStatus_NoApplication_RecruitmentEnded() throws Exception {
		// given
		var auth = setupMemberAndSyncAuth();
		Generation gen = createGeneration();
		createRecruitmentPeriod(
				gen, LocalDateTime.now().minusDays(10), LocalDateTime.now().minusDays(1));

		// when & then
		performAndLog(
				mockMvc.perform(
						get("/api/applications/status")
								.contentType(MediaType.APPLICATION_JSON)
								.with(
										SecurityMockMvcRequestPostProcessors.authentication(
												auth))))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value("SUCCESS"))
				.andExpect(jsonPath("$.data.applicationId").isEmpty())
				.andExpect(jsonPath("$.data.isSubmitted").value(false))
				.andExpect(jsonPath("$.data.isEnd").value(true));
	}

	@Test
	@DisplayName("03. 지원서가 없고 모집 기간이 아직 남은 경우 isEnd는 false를 반환해야한다")
	@WithMockCustomUser
	void getApplicationStatus_NoApplication_RecruitmentOngoing() throws Exception {
		// given
		var auth = setupMemberAndSyncAuth();
		Generation gen = createGeneration();
		createRecruitmentPeriod(
				gen, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(10));

		// when & then
		performAndLog(
				mockMvc.perform(
						get("/api/applications/status")
								.contentType(MediaType.APPLICATION_JSON)
								.with(
										SecurityMockMvcRequestPostProcessors.authentication(
												auth))))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value("SUCCESS"))
				.andExpect(jsonPath("$.data.applicationId").isEmpty())
				.andExpect(jsonPath("$.data.isSubmitted").value(false))
				.andExpect(jsonPath("$.data.isEnd").value(false));
	}

	@Test
	@DisplayName("04. 작성 중인 지원서가 있고 모집 기간이 남은 경우 applicationId와 isSubmitted=false, isEnd=false를 반환해야한다")
	@WithMockCustomUser
	void getApplicationStatus_InProgress() throws Exception {
		// given
		var auth = setupMemberAndSyncAuth();
		User userEntity = userRepository
				.findById(((CustomUserDetails) auth.getPrincipal()).getUserId())
				.orElseThrow();
		Generation gen = createGeneration();
		createRecruitmentPeriod(
				gen, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(10));

		// 작성 중 상태의 지원서 저장
		Application app = Application.createNew(userEntity, gen);
		applicationRepository.saveAndFlush(app);

		// when & then
		performAndLog(
				mockMvc.perform(
						get("/api/applications/status")
								.contentType(MediaType.APPLICATION_JSON)
								.with(
										SecurityMockMvcRequestPostProcessors.authentication(
												auth))))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value("SUCCESS"))
				.andExpect(jsonPath("$.data.applicationId").value(app.getId()))
				.andExpect(jsonPath("$.data.isSubmitted").value(false))
				.andExpect(jsonPath("$.data.isEnd").value(false));
	}

	@Test
	@DisplayName("05. 제출 완료된 지원서가 있고 모집이 종료된 경우 applicationId와 isSubmitted=true, isEnd=true를 반환해야한다")
	@WithMockCustomUser
	void getApplicationStatus_Submitted() throws Exception {
		// given
		var auth = setupMemberAndSyncAuth();
		User userEntity = userRepository
				.findById(((CustomUserDetails) auth.getPrincipal()).getUserId())
				.orElseThrow();
		Generation gen = createGeneration();
		createRecruitmentPeriod(
				gen, LocalDateTime.now().minusDays(10), LocalDateTime.now().minusDays(1));

		// 제출 완료 상태의 지원서 저장
		Application app = Application.createNew(userEntity, gen);
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
						get("/api/applications/status")
								.contentType(MediaType.APPLICATION_JSON)
								.with(
										SecurityMockMvcRequestPostProcessors.authentication(
												auth))))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value("SUCCESS"))
				.andExpect(jsonPath("$.data.applicationId").value(app.getId()))
				.andExpect(jsonPath("$.data.isSubmitted").value(true))
				.andExpect(jsonPath("$.data.isEnd").value(true));
	}

	// --------------------------------------------------------
	// Helper Methods
	// --------------------------------------------------------

	/** 멤버를 DB에 저장하고, 생성된 진짜 ID로 Authentication을 반환합니다. */
	private UsernamePasswordAuthenticationToken setupMemberAndSyncAuth() {
		// 1. 실제 DB 저장 (여기서 ID 자동 생성)
		User user = userRepository.saveAndFlush(
				User.createGoogleUser("test@gmail.com", "testUser", "123456"));

		// 2. CustomUserDetails 생성 (진짜 ID 주입)
		CustomUserDetails userDetails = new CustomUserDetails(
				user.getId(),
				user.getEmail(), // 혹은 username
				User.Role.APPLICANT);

		// 3. Auth Token 생성
		return new UsernamePasswordAuthenticationToken(
				userDetails, null, userDetails.getAuthorities());
	}

	private Generation createGeneration() {
		Generation newGeneration = Generation.builder()
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
