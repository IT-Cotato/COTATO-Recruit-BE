package org.cotato.backend.recruit.domain.question.entity;

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
import org.cotato.backend.recruit.domain.application.enums.AnswerType;
import org.cotato.backend.recruit.domain.generation.entity.Generation;
import org.cotato.backend.recruit.domain.question.enums.QuestionType;

@Entity
@Getter
@Table(name = "questions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Question {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "question_id", nullable = false)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "generation_id", nullable = false)
	private Generation generation;

	@Column(name = "sequence", nullable = false)
	private Integer sequence;

	@Column(name = "content", nullable = false)
	private String content;

	@Enumerated(EnumType.STRING)
	@Column(name = "question_type", nullable = false)
	private QuestionType questionType;

	@Enumerated(EnumType.STRING)
	@Column(name = "answer_type", nullable = false)
	private AnswerType answerType;

	@Column(name = "max_byte", nullable = false)
	private Integer maxByte;

	@Builder
	public Question(
			Generation generation,
			Integer sequence,
			String content,
			QuestionType questionType,
			AnswerType answerType,
			Integer maxByte) {
		this.generation = generation;
		this.sequence = sequence;
		this.content = content;
		this.questionType = questionType;
		this.answerType = answerType;
		this.maxByte = maxByte;
	}
}
