package pl.m2manager.security.auth;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import pl.m2manager.user.entity.User;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class AuthenticatedUser implements UserDetails {

	private final UUID userId;
	private final UUID organizationId;
	private final String email;
	private final String passwordHash;
	private final boolean active;

	public AuthenticatedUser(UUID userId, UUID organizationId, String email, String passwordHash, boolean active) {
		this.userId = userId;
		this.organizationId = organizationId;
		this.email = email;
		this.passwordHash = passwordHash;
		this.active = active;
	}

	public static AuthenticatedUser from(User user) {
		return new AuthenticatedUser(
				user.getId(),
				user.getOrganization().getId(),
				user.getEmail(),
				user.getPasswordHash(),
				user.isActive()
		);
	}

	public UUID getUserId() {
		return userId;
	}

	public UUID getOrganizationId() {
		return organizationId;
	}

	public String getEmail() {
		return email;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of();
	}

	@Override
	public String getPassword() {
		return passwordHash;
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
		return active;
	}
}
