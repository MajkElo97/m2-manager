package pl.m2manager.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class UserOrganizationId implements Serializable {

	@Column(name = "user_id", nullable = false)
	private UUID userId;

	@Column(name = "organization_id", nullable = false)
	private UUID organizationId;

	protected UserOrganizationId() {
	}

	public UserOrganizationId(UUID userId, UUID organizationId) {
		this.userId = userId;
		this.organizationId = organizationId;
	}

	public UUID getUserId() {
		return userId;
	}

	public UUID getOrganizationId() {
		return organizationId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof UserOrganizationId that)) {
			return false;
		}
		return Objects.equals(userId, that.userId) && Objects.equals(organizationId, that.organizationId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(userId, organizationId);
	}
}
