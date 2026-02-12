package org.cotato.backend.recruit.admin.loadtest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 부하 테스트용 Service
 * DB Connection Pool 고갈 시뮬레이션을 위해 Thread.sleep()을 사용
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoadTestService {

    private final LoadTestRepository loadTestRepository;

    /**
     * 부하 테스트 처리
     * 1. CPU 부하: 문자열 길이 계산
     * 2. DB 저장
     * 3. Connection 점유: Thread.sleep(1000)으로 1초간 DB Connection 점유
     */
    @Transactional
    public LoadTestResponse processLoadTest(LoadTestRequest request) {
        // 1. CPU 부하 시뮬레이션: 문자열 길이 계산
        String content = request.content();
        int contentLength = content != null ? content.length() : 0;
        log.info("Load test - Content length: {}", contentLength);

        // 2. DB에 저장
        LoadTest loadTest = LoadTest.create(content, contentLength);
        LoadTest savedLoadTest = loadTestRepository.save(loadTest);
        log.info("Load test - Saved entity with ID: {}", savedLoadTest.getId());

        // 3. DB Connection 점유 시뮬레이션 (Connection Pool 고갈 테스트)
        try {
            log.info("Load test - Holding DB connection for 1 second...");
            Thread.sleep(1000); // 1초간 대기 (트랜잭션 활성 상태 유지)
            log.info("Load test - Connection released after 1 second");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Load test - Thread interrupted during sleep");
        }

        return LoadTestResponse.of(savedLoadTest.getId(), savedLoadTest.getContentLength());
    }
}
