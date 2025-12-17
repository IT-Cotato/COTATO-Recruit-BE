package org.cotato.backend.recruit.domain.user.repository;

import java.util.Optional;
import org.cotato.backend.recruit.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByEmail(String email);

	/**
	 * Provider와 Provider ID로 사용자 조회
	 *
	 * @param provider OAuth2 제공자
	 * @param providerId Provider에서 제공하는 사용자 ID
	 * @return 사용자 (Optional)
	 */
	Optional<User> findByProviderAndProviderId(User.Provider provider, String providerId);

	boolean existsByEmail(String email);
}
