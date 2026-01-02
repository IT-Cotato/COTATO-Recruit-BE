package org.cotato.backend.recruit.admin.service.passerManagement;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.cotato.backend.recruit.admin.dto.response.passer.PassStatusSummaryResponse;
import org.cotato.backend.recruit.admin.service.application.ApplicationAdminService;
import org.cotato.backend.recruit.domain.application.enums.PassStatus;
import org.cotato.backend.recruit.domain.question.enums.PartType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PasserSummaryService {

	private final ApplicationAdminService applicationAdminService;

	public List<PassStatusSummaryResponse> getPasserSummary(Long generationId) {
		List<Object[]> results = applicationAdminService.getPassStatusCounts(generationId);

		Map<PassStatus, Map<String, Long>> statusMap = new EnumMap<>(PassStatus.class);
		List<PassStatus> targetStatuses =
				List.of(PassStatus.PASS, PassStatus.WAITLISTED, PassStatus.FAIL);

		// statusMap 초기화
		initializeStatusMap(statusMap, targetStatuses);

		// DB 결과를 statusMap에 매핑
		mapStatusMap(results, statusMap, targetStatuses);

		// ALL 결과 계산
		calculateAllCounts(statusMap);

		// responseList 생성
		List<PassStatusSummaryResponse> responseList = getResponseList(statusMap);

		return responseList;
	}

	// 응답 형태 매핑
	private List<PassStatusSummaryResponse> getResponseList(
			Map<PassStatus, Map<String, Long>> statusMap) {
		List<PassStatusSummaryResponse> responseList = new ArrayList<>();
		for (Map.Entry<PassStatus, Map<String, Long>> entry : statusMap.entrySet()) {
			PassStatusSummaryResponse response =
					PassStatusSummaryResponse.builder()
							.passStatus(entry.getKey())
							.counts(entry.getValue())
							.build();
			responseList.add(response);
		}
		return responseList;
	}

	private void calculateAllCounts(Map<PassStatus, Map<String, Long>> statusMap) {
		for (PassStatus status : statusMap.keySet()) {
			Map<String, Long> counts = statusMap.get(status);
			long allCount = counts.values().stream().mapToLong(Long::longValue).sum();
			counts.put("ALL", allCount);
		}
	}

	private void mapStatusMap(
			List<Object[]> results,
			Map<PassStatus, Map<String, Long>> statusMap,
			List<PassStatus> targetStatuses) {
		// DB 결과 예시
		// results = [
		// [PASS, BE, 10],
		// [PASS, FE, 20],
		// [PASS, PM, 30],
		// [PASS, DE, 40],
		// [WAITLISTED, BE, 50],
		// [WAITLISTED, FE, 60].....
		for (Object[] row : results) {
			PassStatus status = (PassStatus) row[0];
			PartType part = (PartType) row[1];
			Long count = (Long) row[2];

			if (status != null && part != null && targetStatuses.contains(status)) {
				Map<String, Long> countMap = statusMap.get(status);
				if (countMap != null && countMap.containsKey(part.name())) {
					countMap.put(part.name(), count);
				}
			}
		}
	}

	private void initializeStatusMap(
			Map<PassStatus, Map<String, Long>> statusMap, List<PassStatus> targetStatuses) {
		for (PassStatus status : targetStatuses) {
			Map<String, Long> countMap = new LinkedHashMap<>();
			// 파트별 초기화
			countMap.put(PartType.BE.name(), 0L);
			countMap.put(PartType.FE.name(), 0L);
			countMap.put(PartType.PM.name(), 0L);
			countMap.put(PartType.DE.name(), 0L);

			// 합격 상태별 초기화
			// 초기화 예시
			// statusMap = {
			// PASS: {BE: 0, FE: 0, PM: 0, DE: 0},
			// WAITLISTED: {BE: 0, FE: 0, PM: 0, DE: 0},
			// FAIL: {BE: 0, FE: 0, PM: 0, DE: 0}
			// }
			statusMap.put(status, countMap);
		}
	}
}
