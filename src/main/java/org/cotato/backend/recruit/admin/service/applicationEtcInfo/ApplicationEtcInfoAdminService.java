package org.cotato.backend.recruit.admin.service.applicationEtcInfo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.cotato.backend.recruit.domain.application.dto.ApplicationEtcData;
import org.cotato.backend.recruit.domain.application.entity.Application;
import org.cotato.backend.recruit.domain.application.entity.ApplicationEtcInfo;
import org.cotato.backend.recruit.domain.application.repository.ApplicationEtcInfoRepository;
import org.cotato.backend.recruit.presentation.error.PresentationErrorCode;
import org.cotato.backend.recruit.presentation.exception.PresentationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApplicationEtcInfoAdminService {

	private final ApplicationEtcInfoRepository applicationEtcInfoRepository;
	private final ObjectMapper objectMapper;

	/**
	 * ApplicationEtcInfo에서 JSON 데이터를 ApplicationEtcData로 변환
	 *
	 * @param application 지원서
	 * @return ApplicationEtcData (없으면 모든 필드 null인 객체 반환)
	 */
	public ApplicationEtcData getEtcData(Application application) {
		Optional<ApplicationEtcInfo> etcInfoOpt =
				applicationEtcInfoRepository.findByApplication(application);

		if (etcInfoOpt.isEmpty() || etcInfoOpt.get().getEtcData() == null) {
			// 기타 정보가 없으면 모든 필드 null인 객체 반환
			return new ApplicationEtcData(null, null, null, null, null, null);
		}

		try {
			return objectMapper.readValue(etcInfoOpt.get().getEtcData(), ApplicationEtcData.class);
		} catch (JsonProcessingException e) {
			throw new PresentationException(PresentationErrorCode.INVALID_JSON_FORMAT);
		}
	}
}
