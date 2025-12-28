package org.cotato.backend.recruit.domain.faq.repository;

import java.util.List;
import org.cotato.backend.recruit.domain.faq.entity.Faq;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FaqRepository extends JpaRepository<Faq, Long> {
	List<Faq> findAllByOrderBySequenceAsc();
}
