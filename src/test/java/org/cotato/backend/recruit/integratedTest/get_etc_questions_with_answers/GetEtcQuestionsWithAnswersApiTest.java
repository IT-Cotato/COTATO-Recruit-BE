package org.cotato.backend.recruit.integratedTest.get_etc_questions_with_answers;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.cotato.backend.recruit.auth.jwt.JwtTokenProvider;
import org.cotato.backend.recruit.excelReport.TestReportManager;
import org.cotato.backend.recruit.presentation.dto.response.EtcAnswerResponse;
import org.cotato.backend.recruit.presentation.dto.response.EtcAnswerResponse.DiscoveryPathQuestion;
import org.cotato.backend.recruit.presentation.dto.response.EtcAnswerResponse.DiscoveryPathQuestion.DiscoveryPathOption;
import org.cotato.backend.recruit.presentation.service.ApplicationEtcInfoService;
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
@ApiMetadata("GET /api/applications/{applicationId}/etc-questions")
class GetEtcQuestionsWithAnswersControllerTest {

	@Autowired private MockMvc mockMvc;

	@Autowired private ApplicationEtcInfoService applicationEtcInfoService;

	@MockitoBean private JwtTokenProvider jwtTokenProvider;

	@Test
	@DisplayName("기타 질문과 저장된 답변을 조회한다")
	@WithMockCustomUser
	void getEtcQuestionsWithAnswers() throws Exception {
		// given
		DiscoveryPathOption option = new DiscoveryPathOption("INSTAGRAM");
		DiscoveryPathQuestion discoveryPath =
				new DiscoveryPathQuestion(List.of(option), "INSTAGRAM");
		given(jwtTokenProvider.validateToken(anyString())).willReturn(true);

		EtcAnswerResponse response =
				new EtcAnswerResponse(
						discoveryPath,
						"병행 활동 없음",
						"불가능한 시간 없음",
						true,
						true,
						true,
						"3월 3일",
						"3월 5일",
						"3월 6일");

		given(applicationEtcInfoService.getEtcAnswers(anyLong(), anyLong())).willReturn(response);

		// when & then
		mockMvc.perform(get("/api/applications/{applicationId}/etc-questions", 1L))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value("P-200"))
				.andExpect(jsonPath("$.data.discoveryPath.selectedAnswer").value("INSTAGRAM"))
				.andExpect(jsonPath("$.data.parallelActivities").value("병행 활동 없음"));
	}
}
