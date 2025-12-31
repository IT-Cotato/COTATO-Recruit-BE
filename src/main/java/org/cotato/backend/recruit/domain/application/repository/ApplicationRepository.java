package org.cotato.backend.recruit.domain.application.repository;

import java.util.List;
import java.util.Optional;
import org.cotato.backend.recruit.domain.application.entity.Application;
import org.cotato.backend.recruit.domain.generation.entity.Generation;
import org.cotato.backend.recruit.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

	Optional<Application> findByUserAndGeneration(User user, Generation generation);

	// --------------------------------------------------------------------------------
	// [1] 목록 조회
	// --------------------------------------------------------------------------------
	@Query(
			value =
					"""
			SELECT
				a.*,
				-- (정렬용) 학교별 지원자 수 계산 (university_count)
				COUNT(a.application_id) OVER (PARTITION BY a.university) as university_count
			FROM
				applications a
			WHERE
				a.generation_id = :generationId

				-- [필터링]
				-- partViewType이 ALL이면 항상 TRUE
				-- 'ALL' = 'ALL'이 되므로 항상 TRUE
				AND (:partViewType = 'ALL' OR a.part_type = :partViewType)
				-- passViewType이 ALL이면 항상 TRUE
				-- 'ALL' = 'ALL'이 되므로 항상 TRUE
				AND (:passViewType = 'ALL' OR a.pass_status = :passViewType)

				-- [검색]
				-- keyword가 NULL, '' 공백이면 항상 TRUE
				AND (
					:keyword IS NULL OR :keyword = ''
					OR a.name LIKE CONCAT('%', :keyword, '%')
					OR a.university LIKE CONCAT('%', :keyword, '%')
				)
			ORDER BY
				-- [1순위] 학교 지원자 수 (파라미터 변경: schoolDir -> universityDir)
				CASE WHEN :universityDir = 'DESC' THEN COUNT(a.application_id) OVER (PARTITION BY a.university) END DESC,
				CASE WHEN :universityDir = 'ASC'  THEN COUNT(a.application_id) OVER (PARTITION BY a.university) END ASC,

				-- [2순위] 이름 오름차순 고정
				a.name ASC
			LIMIT :limit
			OFFSET :offset
			""",
			nativeQuery = true)
	List<Application> findApplicationsWithUniversitySort(
			@Param("generationId") Long generationId,
			@Param("partViewType") String partViewType,
			@Param("passViewType") String passViewType,
			@Param("keyword") String keyword,
			@Param("universityDir") String universityDir, // [변경] schoolDir -> universityDir
			@Param("limit") int limit,
			@Param("offset") long offset);

	// --------------------------------------------------------------------------------
	// [2] 파트별 통계 조회
	// --------------------------------------------------------------------------------
	@Query(
			value =
					"""
			SELECT
				a.part_type,
				COUNT(*)
			FROM
				applications a
			WHERE
				a.generation_id = :generationId
				AND (:partViewType = 'ALL' OR a.part_type = :partViewType)
				AND (:passViewStatus = 'ALL' OR a.pass_status = :passViewStatus)
				AND (
					:keyword IS NULL OR :keyword = ''
					OR a.name LIKE CONCAT('%', :keyword, '%')
					OR a.university LIKE CONCAT('%', :keyword, '%')
				)
			GROUP BY
				a.part_type
			""",
			nativeQuery = true)
	List<Object[]> countByFilterGroupByPartType(
			@Param("generationId") Long generationId,
			@Param("partViewType") String partViewType,
			@Param("passViewStatus") String passViewStatus,
			@Param("keyword") String keyword);

	// --------------------------------------------------------------------------------
	// [3] 전체 개수 조회 - 페이징 계산용
	// --------------------------------------------------------------------------------
	@Query(
			value =
					"""
			SELECT COUNT(*)
			FROM applications a
			WHERE
				a.generation_id = :generationId

				-- [필터링]
				AND (:partViewType = 'ALL' OR a.part_type = :partViewType)
				AND (:passViewType = 'ALL' OR a.pass_status = :passViewType)

				-- [검색]
				AND (
					:keyword IS NULL OR :keyword = ''
					OR a.name LIKE CONCAT('%', :keyword, '%')
					OR a.university LIKE CONCAT('%', :keyword, '%')
				)
			""",
			nativeQuery = true)
	long countApplicationsWithFilter(
			@Param("generationId") Long generationId,
			@Param("partViewType") String partViewType,
			@Param("passViewType") String passViewType,
			@Param("keyword") String keyword);
}
