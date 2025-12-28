package org.cotato.backend.recruit.domain.generation.repository;

import java.util.Optional;
import org.cotato.backend.recruit.domain.generation.entity.Generation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GenerationRepository extends JpaRepository<Generation, Long> {
	Optional<Generation> findById(Long id);

	Generation findByIsRecruitingActiveTrue();
}
