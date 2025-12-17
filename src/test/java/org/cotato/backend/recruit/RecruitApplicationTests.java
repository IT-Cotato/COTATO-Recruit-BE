package org.cotato.backend.recruit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@SpringBootTest

	@TestConfiguration
	static class TestConfig {
		@Bean
		@Primary
		public ObjectMapper objectMapper() {
			return new ObjectMapper();
		}
	}

	@Test
	void contextLoads() {}
}
