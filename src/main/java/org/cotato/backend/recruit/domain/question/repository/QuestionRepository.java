package org.cotato.backend.recruit.domain.question.repository;

import java.util.List;
import org.cotato.backend.recruit.domain.generation.entity.Generation;
import org.cotato.backend.recruit.domain.question.entity.Question;
import org.cotato.backend.recruit.domain.question.enums.PartType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

	/**
	 * 기수와 파트 타입으로 질문 조회 (순서대로 정렬)
	 *
	 * @param generation 기수
	 * @param partType 파트 타입
	 * @return 질문 목록
	 */
	List<Question> findByGenerationAndPartTypeOrderBySequenceAsc(
			Generation generation, PartType partType);
}
