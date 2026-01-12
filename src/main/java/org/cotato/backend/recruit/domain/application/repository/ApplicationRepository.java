package org.cotato.backend.recruit.domain.application.repository;

import java.util.List;
import java.util.Optional;
import org.cotato.backend.recruit.domain.application.entity.Application;
import org.cotato.backend.recruit.domain.application.enums.PassStatus;
import org.cotato.backend.recruit.domain.generation.entity.Generation;
import org.cotato.backend.recruit.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

	/**
	 * 사용자와 기수로 지원서 조회
	 *
	 * @param user 사용자
	 * @param generation 기수
	 * @return 지원서 (Optional)
	 */
	Optional<Application> findByUserAndGeneration(User user, Generation generation);

	/** 특정 기수의 특정 합격 상태를 가진 지원서 목록 조회 */
	List<Application> findByGenerationAndPassStatus(Generation generation, PassStatus passStatus);

	/** 특정 기수의 특정 합격 상태를 가진 지원서 개수 조회 */
	long countByGenerationAndPassStatus(Generation generation, PassStatus passStatus);

	// 목록 조회 (필터링 + 페이징)
	@Query(
			value =
					"""
			SELECT *
			FROM applications a
			WHERE
				a.generation_id = :generationId
				AND (:partViewType = 'ALL' OR a.application_part_type = :partViewType)
				AND ('ALL' IN :passViewStatuses OR a.pass_status IN :passViewStatuses)
				AND (
					:keyword IS NULL OR :keyword = ''
					OR a.name LIKE CONCAT('%', :keyword, '%')
					OR a.university LIKE CONCAT('%', :keyword, '%')
				)
			""",
			countQuery =
					"""
			SELECT COUNT(*)
			FROM applications a
			WHERE
				a.generation_id = :generationId
				AND (:partViewType = 'ALL' OR a.application_part_type = :partViewType)
				AND ('ALL' IN :passViewStatuses OR a.pass_status IN :passViewStatuses)
				AND (
					:keyword IS NULL OR :keyword = ''
					OR a.name LIKE CONCAT('%', :keyword, '%')
					OR a.university LIKE CONCAT('%', :keyword, '%')
				)
			""",
			nativeQuery = true)
	Page<Application> findWithFilters(
			@Param("generationId") Long generationId,
			@Param("partViewType") String partViewType,
			@Param("passViewStatuses") List<String> passViewStatuses,
			@Param("keyword") String keyword,
			Pageable pageable);

	// 파트별 통계 조회
	@Query(
			value =
					"""
			SELECT
				a.application_part_type,
				COUNT(*)
			FROM
				applications a
			WHERE
				a.generation_id = :generationId
				AND ('ALL' IN :passViewStatuses OR a.pass_status IN :passViewStatuses)
				AND (
					:keyword IS NULL OR :keyword = ''
					OR a.name LIKE CONCAT('%', :keyword, '%')
					OR a.university LIKE CONCAT('%', :keyword, '%')
				)
			GROUP BY
				a.application_part_type
			""",
			nativeQuery = true)
	List<Object[]> countByFilterGroupByApplicationPartType(
			@Param("generationId") Long generationId,
			@Param("passViewStatuses") List<String> passViewStatuses,
			@Param("keyword") String keyword);

	// 합격 상태 및 파트별 통계 조회
	@Query(
			"SELECT a.passStatus, a.applicationPartType, COUNT(a) FROM Application a WHERE"
				+ " a.generation.id = :generationId GROUP BY a.passStatus, a.applicationPartType")
	List<Object[]> countByGenerationIdGroupByPassStatusAndApplicationPartType(
			@Param("generationId") Long generationId);
}
