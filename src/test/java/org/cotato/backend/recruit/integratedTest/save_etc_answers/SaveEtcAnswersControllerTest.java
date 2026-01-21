package org.cotato.backend.recruit.integratedTest.save_etc_answers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cotato.backend.recruit.domain.application.enums.DiscoveryPath;
import org.cotato.backend.recruit.excelReport.TestReportManager;
import org.cotato.backend.recruit.presentation.dto.request.EtcAnswersRequest;
import org.cotato.backend.recruit.presentation.service.ApplicationAnswerService;
import org.cotato.backend.recruit.presentation.service.ApplicationEtcInfoService;
import org.cotato.backend.recruit.presentation.service.ApplicationService;
import org.cotato.backend.recruit.testsupport.ApiMetadata;
import org.cotato.backend.recruit.testsupport.WithMockCustomUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(TestReportManager.class)
@ApiMetadata("POST /api/applications/{applicationId}/etc-answers")
class SaveEtcAnswersControllerTest {

	@Autowired private MockMvc mockMvc;

	@Autowired private ObjectMapper objectMapper;

	@MockitoBean private ApplicationService applicationService;

	@MockitoBean private ApplicationAnswerService applicationAnswerService;

	@MockitoBean private ApplicationEtcInfoService applicationEtcInfoService;

	@Test
	@DisplayName("기타 질문 응답을 임시 저장한다")
	@WithMockCustomUser
	void saveEtcAnswers() throws Exception {
		// given
		EtcAnswersRequest request =
				new EtcAnswersRequest(
						DiscoveryPath.SNS, "동아리 활동 없음", "불가능 시간 없음", true, true, true);

		willDoNothing()
				.given(applicationEtcInfoService)
				.saveEtcAnswers(anyLong(), anyLong(), any(EtcAnswersRequest.class));

		// when & then
		mockMvc.perform(
						post("/api/applications/{applicationId}/etc-answers", 1L)
								.with(SecurityMockMvcRequestPostProcessors.csrf())
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value("P-200"));
	}
}
