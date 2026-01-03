package org.cotato.backend.recruit.domain.faq.repository;

import java.util.List;
import org.cotato.backend.recruit.domain.faq.entity.Faq;
import org.cotato.backend.recruit.domain.faq.enums.FaqType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FaqRepository extends JpaRepository<Faq, Long> {
	// 특정 타입만 순서대로 조회
	List<Faq> findAllByFaqTypeOrderBySequenceAsc(FaqType faqType);
}
