package org.cotato.backend.recruit.integratedTest.get_questions_with_answers;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.cotato.backend.recruit.excelReport.TestReportManager;
import org.cotato.backend.recruit.presentation.dto.response.PartQuestionResponse;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(TestReportManager.class)
@ApiMetadata("GET /api/applications/{applicationId}/part-questions")
class GetQuestionsWithAnswersControllerTest {

	@Autowired private MockMvc mockMvc;

	@Autowired private ObjectMapper objectMapper;

	@MockitoBean private ApplicationService applicationService;

	@MockitoBean private ApplicationAnswerService applicationAnswerService;

	@MockitoBean private ApplicationEtcInfoService applicationEtcInfoService;

	@Test
	@DisplayName("파트별 질문과 저장된 답변을 조회한다")
	@WithMockCustomUser
	void getQuestionsWithAnswers() throws Exception {
		// given
		PartQuestionResponse response = new PartQuestionResponse(List.of(), null, null);

		given(applicationAnswerService.getQuestionsWithAnswers(anyLong(), anyLong()))
				.willReturn(response);

		// when & then
		mockMvc.perform(get("/api/applications/{applicationId}/part-questions", 1L))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value("P-200"));
	}
}
