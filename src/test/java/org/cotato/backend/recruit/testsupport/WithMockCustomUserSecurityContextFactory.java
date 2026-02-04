package org.cotato.backend.recruit.testsupport;

import org.cotato.backend.recruit.auth.dto.CustomUserDetails;
import org.cotato.backend.recruit.domain.user.entity.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithMockCustomUserSecurityContextFactory
		implements WithSecurityContextFactory<WithMockCustomUser> {
	@Override
	public SecurityContext createSecurityContext(WithMockCustomUser annotation) {
		SecurityContext context = SecurityContextHolder.createEmptyContext();
		CustomUserDetails principal =
				new CustomUserDetails(
						annotation.userId(),
						annotation.username(),
						User.Role.valueOf(annotation.role()));
		Authentication auth =
				new UsernamePasswordAuthenticationToken(
						principal, "password", principal.getAuthorities());
		context.setAuthentication(auth);
		return context;
	}
}
