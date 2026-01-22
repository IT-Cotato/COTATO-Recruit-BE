package org.cotato.backend.recruit.testsupport;

import java.io.PrintWriter;
import java.io.StringWriter;
import org.cotato.backend.recruit.excelReport.TestReportManager;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

public abstract class LogWriteManager {

	/** MockMvc 요청을 수행하고, 발생한 로그를 캡처하여 ReportManager에 넘겨주는 헬퍼 메서드 */
	protected ResultActions performAndLog(ResultActions resultActions) throws Exception {
		// 1. 로그를 담을 그릇(StringWriter) 생성
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);

		// 2. print()의 출력을 콘솔이 아닌 '그릇'으로 돌림
		resultActions.andDo(MockMvcResultHandlers.print(printWriter));

		// 3. 캡처된 로그를 문자열로 변환하여 TestReportManager(ThreadLocal)에 저장
		String capturedLog = stringWriter.toString();
		TestReportManager.setDetailLog(capturedLog);

		// 4. 체이닝을 위해 ResultActions 그대로 반환
		return resultActions;
	}
}
