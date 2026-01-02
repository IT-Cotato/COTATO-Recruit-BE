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
			SELECT *
			FROM applications a
			WHERE
				a.generation_id = :generationId
				AND (:partViewType = 'ALL' OR a.part_type = :partViewType)
				AND (:passViewStatus = 'ALL' OR a.pass_status = :passViewStatus)
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
				AND (:partViewType = 'ALL' OR a.part_type = :partViewType)
				AND (:passViewStatus = 'ALL' OR a.pass_status = :passViewStatus)
				AND (
					:keyword IS NULL OR :keyword = ''
					OR a.name LIKE CONCAT('%', :keyword, '%')
					OR a.university LIKE CONCAT('%', :keyword, '%')
				)
			""",
			nativeQuery = true)
	org.springframework.data.domain.Page<Application> findWithFilters(
			@Param("generationId") Long generationId,
			@Param("partViewType") String partViewType,
			@Param("passViewStatus") String passViewStatus,
			@Param("keyword") String keyword,
			org.springframework.data.domain.Pageable pageable);

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

	@Query(
			"SELECT a.passStatus, a.partType, COUNT(a) FROM Application a WHERE a.generation.id ="
					+ " :generationId GROUP BY a.passStatus, a.partType")
	List<Object[]> countByGenerationIdGroupByPassStatusAndPartType(
			@Param("generationId") Long generationId);
}
