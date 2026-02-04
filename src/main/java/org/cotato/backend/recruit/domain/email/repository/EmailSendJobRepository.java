package org.cotato.backend.recruit.domain.email.repository;

import java.util.Optional;
import org.cotato.backend.recruit.domain.email.entity.EmailSendJob;
import org.cotato.backend.recruit.domain.email.enums.EmailJobType;
import org.cotato.backend.recruit.domain.generation.entity.Generation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailSendJobRepository extends JpaRepository<EmailSendJob, Long> {

	Optional<EmailSendJob> findTopByGenerationAndJobTypeOrderByCreatedAtDesc(
			Generation generation, EmailJobType jobType);
}
