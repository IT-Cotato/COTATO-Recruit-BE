package org.cotato.backend.recruit.domain.evaluation.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EvaluatorType {
	OPERATOR1("운영진1"),
	OPERATOR2("운영진2"),
	OPERATOR3("운영진3"),
	OPERATOR4("운영진4");

	private final String description;
}
