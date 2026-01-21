package org.cotato.backend.recruit.integratedTest.submit_application;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cotato.backend.recruit.excelReport.TestReportManager;
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
@ApiMetadata("POST /api/applications/{applicationId}/submit")
class SubmitApplicationControllerTest {

	@Autowired private MockMvc mockMvc;

	@Autowired private ObjectMapper objectMapper;

	@MockitoBean private ApplicationService applicationService;

	@MockitoBean private ApplicationAnswerService applicationAnswerService;

	@MockitoBean private ApplicationEtcInfoService applicationEtcInfoService;

	@Test
	@DisplayName("지원서를 최종 제출한다")
	@WithMockCustomUser
	void submitApplication() throws Exception {
		// given
		willDoNothing().given(applicationService).submitApplication(anyLong(), anyLong());

		// when & then
		mockMvc.perform(
						post("/api/applications/{applicationId}/submit", 1L)
								.with(SecurityMockMvcRequestPostProcessors.csrf())
								.contentType(MediaType.APPLICATION_JSON))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value("P-200"));
	}
}
