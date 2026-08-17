package pl.m2manager.role.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "role_permissions")
public class RolePermission {

	@EmbeddedId
	private RolePermissionId id;

	protected RolePermission() {
	}

	public RolePermission(UUID roleId, UUID permissionId) {
		this.id = new RolePermissionId(roleId, permissionId);
	}

	public RolePermissionId getId() {
		return id;
	}
}
