package org.cotato.backend.recruit.admin.service.passerManagement;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.cotato.backend.recruit.admin.dto.response.passer.PassStatusSummaryResponse;
import org.cotato.backend.recruit.admin.service.application.ApplicationAdminService;
import org.cotato.backend.recruit.domain.application.enums.ApplicationPartType;
import org.cotato.backend.recruit.domain.application.enums.PassStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PasserSummaryService {

	private final ApplicationAdminService applicationAdminService;

	private static final List<PassStatus> TARGET_STATUS_ORDER =
			List.of(PassStatus.PASS, PassStatus.WAITLISTED, PassStatus.FAIL);

	private static final EnumSet<PassStatus> TARGET_STATUS_SET =
			EnumSet.copyOf(TARGET_STATUS_ORDER);

	public List<PassStatusSummaryResponse> getPasserSummary(Long generationId) {
		// status -> (part -> count)
		Map<PassStatus, Map<ApplicationPartType, Long>> grouped = aggregate(generationId);

		return TARGET_STATUS_ORDER.stream()
				.map(status -> toResponse(status, grouped.getOrDefault(status, Map.of())))
				.toList();
	}

	// 상태별, 파트별 count를 모아서 Map으로 반환
	private Map<PassStatus, Map<ApplicationPartType, Long>> aggregate(Long generationId) {
		Map<PassStatus, Map<ApplicationPartType, Long>> result = new EnumMap<>(PassStatus.class);

		for (Object[] row : applicationAdminService.getPassStatusCounts(generationId)) {
			// data 예시
			// [PassStatus.WAITLISTED, FE, 40]
			PassStatusCount data = PassStatusCount.from(row);

			// target status가 아닌 경우 continue
			if (!TARGET_STATUS_SET.contains(data.status())) {
				continue;
			}

			// 중복되는 결과가 있을 경우 합산
			result.computeIfAbsent(data.status(), s -> new EnumMap<>(ApplicationPartType.class))
					.merge(data.part(), data.count(), Long::sum);
		}

		return result;
	}

	// 상태별, 파트별 count를 모아서 Response로 반환
	private PassStatusSummaryResponse toResponse(
			PassStatus status, Map<ApplicationPartType, Long> partCounts) {
		Map<String, Long> counts = new LinkedHashMap<>();
		long total = 0L;

		for (ApplicationPartType part : ApplicationPartType.values()) {
			long c = partCounts.getOrDefault(part, 0L);
			counts.put(part.name(), c);
			total += c;
		}

		counts.put("ALL", total);

		return PassStatusSummaryResponse.builder().passStatus(status).counts(counts).build();
	}

	// DB에서 가져온 데이터를 PassStatusCount로 변환
	private record PassStatusCount(PassStatus status, ApplicationPartType part, long count) {
		// 결과 예시
		// [
		// [PassStatus.PASS, BE, 10],
		// [PassStatus.PASS, FE, 20],
		// [PassStatus.WAITLISTED, BE, 30],
		// [PassStatus.WAITLISTED, FE, 40],
		// [PassStatus.FAIL, BE, 50],
		// [PassStatus.FAIL, FE, 60]
		// ]
		static PassStatusCount from(Object[] row) {
			// row[0]=PassStatus, row[1]=ApplicationPartType, row[2]=Long (혹은 Number)
			PassStatus status = (PassStatus) row[0];
			ApplicationPartType part = (ApplicationPartType) row[1];

			long count = (row[2] instanceof Number n) ? n.longValue() : (Long) row[2];
			return new PassStatusCount(status, part, count);
		}
	}
}
