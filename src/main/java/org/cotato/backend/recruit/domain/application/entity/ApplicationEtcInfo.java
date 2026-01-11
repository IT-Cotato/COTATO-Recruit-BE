package org.cotato.backend.recruit.domain.application.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Getter
@Table(name = "application_etc_infos")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApplicationEtcInfo {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "etc_info_id")
	private Long id;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "application_id", nullable = false, unique = true)
	private Application application;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "etc_data", columnDefinition = "json")
	private String etcData;

	// 정적 팩토리 메서드 - 새 기타 정보 생성
	public static ApplicationEtcInfo createNew(Application application) {
		ApplicationEtcInfo etcInfo = new ApplicationEtcInfo();
		etcInfo.application = application;
		return etcInfo;
	}

	// JSON 데이터 업데이트
	public void updateEtcData(String etcData) {
		this.etcData = etcData;
	}
}
