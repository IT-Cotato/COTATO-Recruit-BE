package org.cotato.backend.recruit.admin.loadtest;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 부하 테스트용 Entity
 * DB Connection Pool 고갈 테스트를 위한 더미 데이터 저장
 */
@Entity
@Getter
@Table(name = "load_test")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LoadTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "load_test_id")
    private Long id;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "content_length")
    private Integer contentLength;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // 정적 팩토리 메서드
    public static LoadTest create(String content, Integer contentLength) {
        LoadTest loadTest = new LoadTest();
        loadTest.content = content;
        loadTest.contentLength = contentLength;
        loadTest.createdAt = LocalDateTime.now();
        return loadTest;
    }
}
