package org.cotato.backend.recruit.integratedTest.submitted_application_view;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.cotato.backend.recruit.auth.dto.CustomUserDetails;
import org.cotato.backend.recruit.auth.jwt.JwtTokenProvider;
import org.cotato.backend.recruit.domain.application.entity.Application;
import org.cotato.backend.recruit.domain.application.enums.ApplicationPartType;
import org.cotato.backend.recruit.domain.application.repository.ApplicationRepository;
import org.cotato.backend.recruit.domain.generation.entity.Generation;
import org.cotato.backend.recruit.domain.generation.repository.GenerationRepository;
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
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@ExtendWith(TestReportManager.class)
@ApiMetadata("GET /api/submitted-applications/mypage")
@TestMethodOrder(MethodOrderer.DisplayName.class)
class MyPageApplicationStatusApiTest extends IntegrationTestSupport {

	@Autowired private MockMvc mockMvc;

	@Autowired private UserRepository userRepository;
	@Autowired private GenerationRepository generationRepository;
	@Autowired private ApplicationRepository applicationRepository;

	@MockitoBean private JwtTokenProvider jwtTokenProvider;

	@Test
	@DisplayName("01. 지원서가 없는 경우 빈 리스트를 반환해야 한다")
	@WithMockCustomUser
	void getMyApplications_NoApplication() throws Exception {
		// given
		var auth = setupMemberAndSyncAuth();

		// when & then
		performAndLog(
						mockMvc.perform(
								get("/api/submitted-applications/mypage")
										.with(
												SecurityMockMvcRequestPostProcessors.authentication(
														auth))))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value("SUCCESS"))
				.andExpect(jsonPath("$.data").isEmpty());
	}

	@Test
	@DisplayName("02. 작성 중인 지원서는 반환되지 않아야 한다")
	@WithMockCustomUser
	void getMyApplications_Writing() throws Exception {
		// given
		var auth = setupMemberAndSyncAuth();
		User user =
				userRepository
						.findById(((CustomUserDetails) auth.getPrincipal()).getUserId())
						.orElseThrow();
		Generation gen = createGeneration(10); // 10기

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
		applicationRepository.save(app);

		// when & then
		performAndLog(
						mockMvc.perform(
								get("/api/submitted-applications/mypage")
										.with(
												SecurityMockMvcRequestPostProcessors.authentication(
														auth))))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value("SUCCESS"))
				.andExpect(jsonPath("$.data").isEmpty());
	}

	@Test
	@DisplayName("03. 제출 완료된 지원서가 있는 경우 '지원완료' 상태로 반환해야 한다")
	@WithMockCustomUser
	void getMyApplications_Submitted() throws Exception {
		// given
		var auth = setupMemberAndSyncAuth();
		// ... existing setup ...
		User user =
				userRepository
						.findById(((CustomUserDetails) auth.getPrincipal()).getUserId())
						.orElseThrow();
		Generation gen = createGeneration(11); // 11기

		Application app = Application.createNew(user, gen);
		app.updateBasicInfo(
				"test",
				"FEMALE",
				java.time.LocalDate.now(),
				"010-1111-2222",
				"Univ2",
				"Major2",
				2,
				true,
				false,
				ApplicationPartType.FE);
		// 제출 처리 (submit 메소드 사용)
		app.submit(List.of());
		applicationRepository.save(app);

		// when & then
		performAndLog(
						mockMvc.perform(
								get("/api/submitted-applications/mypage")
										.with(
												SecurityMockMvcRequestPostProcessors.authentication(
														auth))))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value("SUCCESS"))
				.andExpect(jsonPath("$.data[0].applicationId").value(app.getId()))
				.andExpect(jsonPath("$.data[0].generationNumber").value(11))
				.andExpect(jsonPath("$.data[0].part").value("FE"))
				.andExpect(jsonPath("$.data[0].status").value("지원완료"));
	}

	@Test
	@DisplayName("04. 여러 기수의 지원서 중 제출된 것만 반환해야 한다")
	@WithMockCustomUser
	void getMyApplications_Multiple() throws Exception {
		// given
		var auth = setupMemberAndSyncAuth();
		User user =
				userRepository
						.findById(((CustomUserDetails) auth.getPrincipal()).getUserId())
						.orElseThrow();

		Generation gen10 = createGeneration(10);
		Generation gen11 = createGeneration(11);

		// 10기 지원서 (작성중, 미제출)
		Application app1 = Application.createNew(user, gen10);
		app1.updateBasicInfo(
				"test",
				"MALE",
				java.time.LocalDate.now(),
				"010-0000-0000",
				"Univ",
				"Major",
				1,
				false,
				true,
				ApplicationPartType.DE);
		applicationRepository.save(app1);

		// 11기 지원서 (제출완료)
		Application app2 = Application.createNew(user, gen11);
		app2.updateBasicInfo(
				"test",
				"MALE",
				java.time.LocalDate.now(),
				"010-0000-0000",
				"Univ",
				"Major",
				1,
				false,
				true,
				ApplicationPartType.PM);
		app2.submit(List.of());
		applicationRepository.save(app2);

		// when & then
		performAndLog(
						mockMvc.perform(
								get("/api/submitted-applications/mypage")
										.with(
												SecurityMockMvcRequestPostProcessors.authentication(
														auth))))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value("SUCCESS"))
				.andExpect(jsonPath("$.data.length()").value(1))
				.andExpect(jsonPath("$.data[0].applicationId").value(app2.getId()));
	}

	// --------------------------------------------------------
	// Helper Methods
	// --------------------------------------------------------

	private UsernamePasswordAuthenticationToken setupMemberAndSyncAuth() {
		User user =
				userRepository.save(User.createGoogleUser("test@gmail.com", "testUser", "123456"));
		CustomUserDetails userDetails =
				new CustomUserDetails(user.getId(), user.getEmail(), User.Role.APPLICANT);
		return new UsernamePasswordAuthenticationToken(
				userDetails, null, userDetails.getAuthorities());
	}

	private Generation createGeneration(int number) {
		return generationRepository.save(
				Generation.builder()
						.id((long) number)
						.isRecruitingActive(true)
						.isAdditionalRecruitmentActive(false)
						.build());
	}
}
