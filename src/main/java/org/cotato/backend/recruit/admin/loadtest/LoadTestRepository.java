package org.cotato.backend.recruit.admin.loadtest;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 부하 테스트용 Repository
 */
public interface LoadTestRepository extends JpaRepository<LoadTest, Long> {
}
