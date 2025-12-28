package org.cotato.backend.recruit.domain.application.repository;

import java.util.List;
import java.util.Optional;
import org.cotato.backend.recruit.domain.application.entity.Application;
import org.cotato.backend.recruit.domain.application.entity.ApplicationAnswer;
import org.cotato.backend.recruit.domain.question.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationAnswerRepository extends JpaRepository<ApplicationAnswer, Long> {

	/**
	 * 지원서로 모든 답변 조회
	 *
	 * @param application 지원서
	 * @return 답변 목록
	 */
	List<ApplicationAnswer> findByApplication(Application application);

	/**
	 * 지원서와 질문으로 답변 조회
	 *
	 * @param application 지원서
	 * @param question 질문
	 * @return 답변 (Optional)
	 */
	Optional<ApplicationAnswer> findByApplicationAndQuestion(
			Application application, Question question);
}
