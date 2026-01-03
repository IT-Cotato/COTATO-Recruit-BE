package org.cotato.backend.recruit.auth.dto;

import lombok.Builder;
import org.cotato.backend.recruit.domain.user.entity.User;

@Builder
public record UserInfoResponse(Long userId, String email, String name, String role) {

	public static UserInfoResponse from(User user) {
		return UserInfoResponse.builder()
				.userId(user.getId())
				.email(user.getEmail())
				.name(user.getName())
				.role(user.getRole().name())
				.build();
	}
}
