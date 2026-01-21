package org.cotato.backend.recruit.testsupport;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 핵심: TYPE만 남겨서 클래스 레벨 전용으로 강제합니다.
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiMetadata {
	String value();
}
