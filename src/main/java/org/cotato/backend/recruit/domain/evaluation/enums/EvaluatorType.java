package org.cotato.backend.recruit.domain.evaluation.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EvaluatorType {
	STAFF1("운영진1"),
	STAFF2("운영진2"),
	STAFF3("운영진3"),
	STAFF4("운영진4");

	private final String description;
}
