package org.cotato.backend.recruit.common.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.concurrent.TimeUnit;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

	@Bean
	public CacheManager cacheManager() {
		CaffeineCacheManager cacheManager = new CaffeineCacheManager();

		// activeGeneration 캐시: 1시간 TTL
		cacheManager.registerCustomCache(
				"activeGeneration",
				Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).maximumSize(10).build());

		// latestGeneration 캐시: 1시간 TTL
		cacheManager.registerCustomCache(
				"latestGeneration",
				Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).maximumSize(10).build());

		// questions 캐시: 1일 TTL
		cacheManager.registerCustomCache(
				"questions",
				Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.DAYS).maximumSize(100).build());

		// question (단일 질문) 캐시: 1일 TTL
		cacheManager.registerCustomCache(
				"question",
				Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.DAYS).maximumSize(1000).build());

		// recruitmentSchedule 캐시: 1시간 TTL
		cacheManager.registerCustomCache(
				"recruitmentSchedule",
				Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).maximumSize(10).build());

		// recruitmentStatus 캐시: 1시간 TTL
		cacheManager.registerCustomCache(
				"recruitmentStatus",
				Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).maximumSize(10).build());

		return cacheManager;
	}
}
