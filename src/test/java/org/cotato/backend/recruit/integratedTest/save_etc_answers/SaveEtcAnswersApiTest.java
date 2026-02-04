package org.cotato.backend.recruit.integratedTest.save_etc_answers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import org.cotato.backend.recruit.auth.dto.CustomUserDetails;
import org.cotato.backend.recruit.auth.jwt.JwtTokenProvider;
import org.cotato.backend.recruit.domain.application.dto.ApplicationEtcData;
import org.cotato.backend.recruit.domain.application.entity.Application;
import org.cotato.backend.recruit.domain.application.entity.ApplicationEtcInfo;
import org.cotato.backend.recruit.domain.application.enums.DiscoveryPath;
import org.cotato.backend.recruit.domain.application.repository.ApplicationEtcInfoRepository;
import org.cotato.backend.recruit.domain.application.repository.ApplicationRepository;
import org.cotato.backend.recruit.domain.generation.entity.Generation;
import org.cotato.backend.recruit.domain.generation.repository.GenerationRepository;
import org.cotato.backend.recruit.domain.recruitmentInformation.entity.RecruitmentInformation;
import org.cotato.backend.recruit.domain.recruitmentInformation.enums.InformationType;
import org.cotato.backend.recruit.domain.recruitmentInformation.repository.RecruitmentInformationRepository;
import org.cotato.backend.recruit.domain.user.entity.User;
import org.cotato.backend.recruit.domain.user.repository.UserRepository;
import org.cotato.backend.recruit.excelReport.TestReportManager;
import org.cotato.backend.recruit.presentation.dto.request.EtcAnswersRequest;
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
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@ExtendWith(TestReportManager.class)
@ApiMetadata("POST /api/applications/{applicationId}/etc-answers")
@TestMethodOrder(MethodOrderer.DisplayName.class)
class SaveEtcAnswersApiTest extends IntegrationTestSupport {

	@Autowired private MockMvc mockMvc;

	@MockitoSpyBean private ObjectMapper objectMapper;

	@Autowired private UserRepository userRepository;
	@Autowired private GenerationRepository generationRepository;
	@Autowired private ApplicationRepository applicationRepository;
	@Autowired private ApplicationEtcInfoRepository applicationEtcInfoRepository;
	@Autowired private RecruitmentInformationRepository recruitmentInformationRepository;

	@MockitoBean private JwtTokenProvider jwtTokenProvider;

	@Test
	@DisplayName("01. 지원서를 찾지 못하면 예외처리해야한다")
	@WithMockCustomUser
	void saveEtcAnswers_AppNotFound() throws Exception {
		// given
		var auth = setupMemberAndSyncAuth();
		EtcAnswersRequest request =
				new EtcAnswersRequest(
						DiscoveryPath.INSTAGRAM, "No activity", "No time", true, true, true);

		// when & then
		performAndLog(
				mockMvc.perform(
								post("/api/applications/{applicationId}/etc-answers", 9999L)
										.with(
												SecurityMockMvcRequestPostProcessors.authentication(
														auth))
										.with(SecurityMockMvcRequestPostProcessors.csrf())
										.contentType(MediaType.APPLICATION_JSON)
										.content(
												objectMapper.writeValueAsString(
														request))) // Real ObjectMapper
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
	void saveEtcAnswers_Forbidden() throws Exception {
		// given
		var auth = setupMemberAndSyncAuth();
		User otherUser =
				userRepository.saveAndFlush(
						User.createGoogleUser("other@gmail.com", "other", "123"));
		Generation gen = createGeneration();
		createRecruitmentPeriod(gen);
		Application app = Application.createNew(otherUser, gen);
		applicationRepository.saveAndFlush(app);

		EtcAnswersRequest request =
				new EtcAnswersRequest(
						DiscoveryPath.INSTAGRAM, "No activity", "No time", true, true, true);

		// when & then
		mockMvc.perform(
						post("/api/applications/{applicationId}/etc-answers", app.getId())
								.with(SecurityMockMvcRequestPostProcessors.authentication(auth))
								.with(SecurityMockMvcRequestPostProcessors.csrf())
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isForbidden())
				.andExpect(
						jsonPath("$.code")
								.value(PresentationErrorCode.APPLICATION_FORBIDDEN.getCode()));
	}

	@Test
	@DisplayName("03. 기타정보 format이 json이 아니면 예외처리해야한다")
	@WithMockCustomUser
	void saveEtcAnswers_InvalidJson() throws Exception {
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

		EtcAnswersRequest request =
				new EtcAnswersRequest(
						DiscoveryPath.INSTAGRAM, "No activity", "No time", true, true, true);

		// Force serialization error
		given(objectMapper.writeValueAsString(any(ApplicationEtcData.class)))
				.willThrow(new JsonProcessingException("Simulated Error") {});

		// when & then
		// Note: The controller calls service.saveEtcAnswers ->
		// objectMapper.writeValueAsString(etcData)
		// We can't use objectMapper to write REQUEST body if we mock it to fail for
		// etcData?
		// Wait, we are mocking writeValueAsString(ApplicationEtcData.class).
		// The request body is EtcAnswersRequest.
		// So `mockMvc` content creation (writeValueAsString(request)) should follow
		// real implementation OR we need to distinguish.
		// If `objectMapper` is Spy, we can allow
		// `writeValueAsString(EtcAnswersRequest)` to work, and fail
		// `writeValueAsString(ApplicationEtcData)`.

		// However, `any(ApplicationEtcData.class)` signature might overlap if `any()`
		// is used.
		// Let's use `given` carefully.

		// Wait, if I use `objectMapper.writeValueAsString(request)` in the test setup
		// line below, it might trigger the mock if I'm not careful.
		// But I defined expectation on `any(ApplicationEtcData.class)`.
		// `EtcAnswersRequest` is different class. So it should be fine.

		performAndLog(
				mockMvc.perform(
								post("/api/applications/{applicationId}/etc-answers", app.getId())
										.with(
												SecurityMockMvcRequestPostProcessors.authentication(
														auth))
										.with(SecurityMockMvcRequestPostProcessors.csrf())
										.contentType(MediaType.APPLICATION_JSON)
										// Use a new ObjectMapper for test setup to avoid side
										// effects if
										// Spy behaves
										// weirdly
										.content(new ObjectMapper().writeValueAsString(request)))
						.andDo(print())
						.andExpect(status().isInternalServerError())
						.andExpect(
								jsonPath("$.code")
										.value(
												PresentationErrorCode.INVALID_JSON_FORMAT
														.getCode())));
	}

	@Test
	@DisplayName("04. 기타정보가 존재하지않으면 새롭게 생성해야한다")
	@WithMockCustomUser
	void saveEtcAnswers_Success() throws Exception {
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

		EtcAnswersRequest request =
				new EtcAnswersRequest(
						DiscoveryPath.INSTAGRAM, "Activity", "Time", true, true, true);

		// when & then
		performAndLog(
				mockMvc.perform(
								post("/api/applications/{applicationId}/etc-answers", app.getId())
										.with(
												SecurityMockMvcRequestPostProcessors.authentication(
														auth))
										.with(SecurityMockMvcRequestPostProcessors.csrf())
										.contentType(MediaType.APPLICATION_JSON)
										.content(new ObjectMapper().writeValueAsString(request)))
						.andDo(print())
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.code").value("SUCCESS")));

		// Verify DB
		ApplicationEtcInfo etcInfo =
				applicationEtcInfoRepository.findByApplication(app).orElseThrow();
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
