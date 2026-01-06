package org.cotato.backend.recruit.domain.evaluation.repository;

import java.util.Optional;
import org.cotato.backend.recruit.domain.application.entity.Application;
import org.cotato.backend.recruit.domain.evaluation.entity.Evaluation;
import org.cotato.backend.recruit.domain.evaluation.enums.EvaluatorType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {
	Optional<Evaluation> findByApplicationAndEvaluatorType(
			Application application, EvaluatorType evaluatorType);
}
