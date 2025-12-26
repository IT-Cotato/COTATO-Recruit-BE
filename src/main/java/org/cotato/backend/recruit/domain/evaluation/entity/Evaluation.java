package org.cotato.backend.recruit.domain.evaluation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.cotato.backend.recruit.domain.application.entity.Application;
import org.cotato.backend.recruit.domain.evaluation.enums.EvaluatorType;

@Entity
@Getter
@Table(name = "evaluations")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Evaluation {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "evaluation_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "application_id", nullable = false)
	private Application application;

	@Enumerated(EnumType.STRING)
	@Column(name = "evaluator_type", nullable = false)
	private EvaluatorType evaluatorType;

	@Column(name = "comment", nullable = true)
	private String comment;

	@Builder
	public Evaluation(Application application, EvaluatorType evaluatorType, String comment) {
		this.application = application;
		this.evaluatorType = evaluatorType;
		this.comment = comment;
	}
}
