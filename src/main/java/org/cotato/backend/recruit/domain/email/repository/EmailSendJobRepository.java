package org.cotato.backend.recruit.domain.email.repository;

import org.cotato.backend.recruit.domain.email.entity.EmailSendJob;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailSendJobRepository extends JpaRepository<EmailSendJob, Long> {}
