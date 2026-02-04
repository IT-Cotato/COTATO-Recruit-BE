package org.cotato.backend.recruit.testsupport;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public abstract class IntegrationTestSupport extends LogWriteManager {

	@Autowired private DatabaseCleaner databaseCleaner;

	@BeforeEach
	void setUp() {
		// 매 테스트 메서드 시작 전에 DB를 싹 비웁니다.
		databaseCleaner.execute();
	}
}
