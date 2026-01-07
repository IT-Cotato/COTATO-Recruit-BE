package org.cotato.backend.recruit.common.config;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/** AWS S3 설정 클래스 */
@Slf4j
@Configuration
public class S3Config {

	@Value("${cloud.aws.credentials.access-key}")
	private String accessKey;

	@Value("${cloud.aws.credentials.secret-key}")
	private String secretKey;

	@Value("${cloud.aws.region.static}")
	private String region;

	private S3Presigner s3Presigner;

	@Bean
	public S3Presigner s3Presigner() {
		AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);

		s3Presigner =
				S3Presigner.builder()
						.region(Region.of(region))
						.credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
						.build();

		return s3Presigner;
	}

	/** 애플리케이션 종료 시 S3Presigner 리소스 정리 */
	@PreDestroy
	public void cleanup() {
		if (s3Presigner != null) {
			try {
				s3Presigner.close();
				log.info("S3Presigner 리소스가 정리되었습니다.");
			} catch (Exception e) {
				log.error("S3Presigner 리소스 정리 중 오류 발생", e);
			}
		}
	}
}
