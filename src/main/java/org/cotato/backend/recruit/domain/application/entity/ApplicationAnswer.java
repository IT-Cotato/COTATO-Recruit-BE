package org.cotato.backend.recruit.domain.application.entity;

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
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.cotato.backend.recruit.domain.application.enums.AnswerType;
import org.cotato.backend.recruit.domain.question.entity.Question;

@Entity
@Getter
@Table(name = "application_answers")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApplicationAnswer {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "answer_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "application_id", nullable = false)
	private Application application;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "question_id", nullable = false)
	private Question question;

	@Enumerated(EnumType.STRING)
	@Column(name = "answer_type", nullable = false)
	private AnswerType answerType;

	@Column(name = "is_checked")
	private Boolean isChecked;

	@Column(name = "content")
	private String content;

	@Column(name = "file_key")
	private String fileKey;

	@Column(name = "file_url")
	private String fileUrl;

	// 정적 팩토리 메서드 - 새 답변 생성
	public static ApplicationAnswer of(
			Application application,
			Question question,
			AnswerType answerType,
			Boolean isChecked,
			String content,
			String fileKey,
			String fileUrl) {
		ApplicationAnswer answer = new ApplicationAnswer();
		answer.application = application;
		answer.question = question;
		answer.answerType = answerType;
		answer.isChecked = isChecked;
		answer.content = content;
		answer.fileKey = fileKey;
		answer.fileUrl = fileUrl;
		return answer;
	}

	// 답변 업데이트
	public void update(
			AnswerType answerType,
			Boolean isChecked,
			String content,
			String fileKey,
			String fileUrl) {
		this.answerType = answerType;
		this.isChecked = isChecked;
		this.content = content;
		this.fileKey = fileKey;
		this.fileUrl = fileUrl;
	}
}
