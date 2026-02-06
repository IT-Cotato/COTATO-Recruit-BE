package org.cotato.backend.recruit.integratedTest.get_etc_questions_with_answers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import org.cotato.backend.recruit.auth.dto.CustomUserDetails;
import org.cotato.backend.recruit.auth.jwt.JwtTokenProvider;
import org.cotato.backend.recruit.domain.application.dto.ApplicationEtcData;
import org.cotato.backend.recruit.domain.application.entity.Application;
import org.cotato.backend.recruit.domain.application.entity.ApplicationEtcInfo;
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
@ApiMetadata("GET /api/applications/{applicationId}/etc-questions")
@TestMethodOrder(MethodOrderer.DisplayName.class)
class GetEtcQuestionsWithAnswersApiTest extends IntegrationTestSupport {

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private GenerationRepository generationRepository;
	@Autowired
	private ApplicationRepository applicationRepository;
	@Autowired
	private ApplicationEtcInfoRepository applicationEtcInfoRepository;
	@Autowired
	private RecruitmentInformationRepository recruitmentInformationRepository;

	@MockitoBean
	private JwtTokenProvider jwtTokenProvider;

	// @Test
	// @DisplayName("01. 현재 활성화 된 기수가 없으면 예외처리해야한다")
	// @WithMockCustomUser
	// void getEtcAnswer_NoGeneration() throws Exception {
	// // given
	// var auth = setupMemberAndSyncAuth();
	// User user =
	// userRepository
	// .findById(((CustomUserDetails) auth.getPrincipal()).getUserId())
	// .orElseThrow();

	// // Create an inactive generation
	// Generation gen =
	// generationRepository.saveAndFlush(
	// Generation.builder()
	// .id(1L)
	// .isRecruitingActive(false)
	// .isAdditionalRecruitmentActive(false)
	// .build());

	// Application app = Application.createNew(user, gen);
	// applicationRepository.saveAndFlush(app);

	// // when & then
	// performAndLog(
	// mockMvc.perform(
	// get("/api/applications/{applicationId}/etc-questions", app.getId())
	// .with(
	// SecurityMockMvcRequestPostProcessors.authentication(
	// auth))))
	// .andExpect(status().isNotFound())
	// .andExpect(
	// jsonPath("$.code")
	// .value(PresentationErrorCode.GENERATION_NOT_FOUND.getCode()));
	// }

	@Test
	@DisplayName("02. 기타질문 조회에 성공하면 EtcAnswerResponse를 반환해야한다")
	@WithMockCustomUser
	void getEtcAnswer_Success() throws Exception {
		// given
		var auth = setupMemberAndSyncAuth();
		User user = userRepository
				.findById(((CustomUserDetails) auth.getPrincipal()).getUserId())
				.orElseThrow();

		Generation gen = createGeneration();
		createRecruitmentPeriod(gen);

		Application app = Application.createNew(user, gen);
		applicationRepository.saveAndFlush(app);
		ApplicationEtcData data = new ApplicationEtcData(null, "No activity", null, true, true, true);
		String json = objectMapper.writeValueAsString(data);

		ApplicationEtcInfo etcInfo = ApplicationEtcInfo.createNew(app);
		etcInfo.updateEtcData(json);
		applicationEtcInfoRepository.saveAndFlush(etcInfo);

		// when & then
		performAndLog(
				mockMvc.perform(
						get("/api/applications/{applicationId}/etc-questions", app.getId())
								.with(
										SecurityMockMvcRequestPostProcessors.authentication(
												auth))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value("SUCCESS"))
				.andExpect(jsonPath("$.data.parallelActivities").value("No activity"));
	}

	private UsernamePasswordAuthenticationToken setupMemberAndSyncAuth() {
		User user = userRepository.saveAndFlush(
				User.createGoogleUser("test@gmail.com", "testUser", "123456"));
		CustomUserDetails userDetails = new CustomUserDetails(user.getId(), user.getEmail(), User.Role.APPLICANT);
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
		recruitmentInformationRepository.saveAndFlush(
				RecruitmentInformation.builder()
						.generation(gen)
						.informationType(InformationType.INTERVIEW_START)
						.eventDatetime(LocalDateTime.now().plusDays(5))
						.build());
		recruitmentInformationRepository.saveAndFlush(
				RecruitmentInformation.builder()
						.generation(gen)
						.informationType(InformationType.INTERVIEW_END)
						.eventDatetime(LocalDateTime.now().plusDays(6))
						.build());
		recruitmentInformationRepository.saveAndFlush(
				RecruitmentInformation.builder()
						.generation(gen)
						.informationType(InformationType.OT)
						.eventDatetime(LocalDateTime.now().plusDays(10))
						.build());
	}
}
