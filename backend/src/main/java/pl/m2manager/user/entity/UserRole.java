package pl.m2manager.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "user_roles")
public class UserRole {

	@EmbeddedId
	private UserRoleId id;

	@Column(name = "organization_id", nullable = false)
	private UUID organizationId;

	protected UserRole() {
	}

	public UserRole(UUID organizationId, UUID userId, UUID roleId) {
		this.id = new UserRoleId(userId, roleId);
		this.organizationId = organizationId;
	}

	public UserRoleId getId() {
		return id;
	}

	public UUID getOrganizationId() {
		return organizationId;
	}
}
