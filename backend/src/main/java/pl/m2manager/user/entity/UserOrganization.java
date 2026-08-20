package pl.m2manager.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_organizations")
public class UserOrganization {

	@EmbeddedId
	private UserOrganizationId id;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	protected UserOrganization() {
	}

	public UserOrganization(UUID userId, UUID organizationId, Instant createdAt) {
		this.id = new UserOrganizationId(userId, organizationId);
		this.createdAt = createdAt;
	}

	public UserOrganizationId getId() {
		return id;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
