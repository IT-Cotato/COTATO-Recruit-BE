package org.cotato.backend.recruit.domain.generation.repository;

import org.cotato.backend.recruit.domain.generation.entity.Generation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GenerationRepository extends JpaRepository<Generation, Long> {}
