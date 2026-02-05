package org.cotato.backend.recruit.testsupport;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Table;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DatabaseCleaner implements InitializingBean {

	@PersistenceContext
	private EntityManager entityManager;

	private List<String> tableNames;

	@Override
	public void afterPropertiesSet() {
		// JPA 메타모델을 이용해 모든 테이블 이름을 추출
		tableNames = entityManager.getMetamodel().getEntities().stream()
				.filter(e -> e.getJavaType().getAnnotation(Table.class) != null)
				.map(
						e -> {
							// @Table 어노테이션에서 name 속성을 가져오거나, 없으면 엔티티 이름을 스네이크 케이스로 변환
							String tableName = e.getJavaType().getAnnotation(Table.class).name();
							return tableName.isEmpty()
									? camelToSnake(e.getName())
									: tableName;
						})
				.collect(Collectors.toList());
	}

	@Transactional
	public void execute() {
		// 쓰기 지연 저장소 비우기
		entityManager.flush();
		// 제약 조건 무시 (Foreign Key 에러 방지)
		entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();

		// 모든 테이블 TRUNCATE (데이터 삭제 및 ID 초기화)
		for (String tableName : tableNames) {
			entityManager.createNativeQuery("TRUNCATE TABLE " + tableName).executeUpdate();
		}

		// 제약 조건 다시 활성화
		entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
	}

	// 카멜케이스 -> 스네이크케이스 변환 유틸 (예: ApplicationAnswer -> application_answer)
	private String camelToSnake(String str) {
		return str.replaceAll("([a-z])([A-Z]+)", "$1_$2").toLowerCase();
	}
}
