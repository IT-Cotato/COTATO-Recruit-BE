package org.cotato.backend.recruit.auth.dto;

import java.util.Collection;
import java.util.Collections;
import lombok.Getter;
import org.cotato.backend.recruit.domain.user.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/** Spring Security UserDetails (구현체 인증에 필요한 최소한의 사용자 정보만 포함한다.) */
@Getter
public class CustomUserDetails implements UserDetails {

	private final Long userId;
	private final String email;
	private final User.Role role;

	public CustomUserDetails(Long userId, String email, User.Role role) {
		this.userId = userId;
		this.email = email;
		this.role = role;
	}

	public static CustomUserDetails from(User user) {
		return new CustomUserDetails(user.getId(), user.getEmail(), user.getRole());
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return Collections.singleton(new SimpleGrantedAuthority("ROLE_" + role.name()));
	}

	@Override
	public String getPassword() {
		return null; // OAuth2 사용으로 비밀번호 없음
	}

	@Override
	public String getUsername() {
		return email;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
}
