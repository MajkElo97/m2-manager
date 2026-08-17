package pl.m2manager.role.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.m2manager.permission.entity.Permission;
import pl.m2manager.role.entity.RolePermission;
import pl.m2manager.role.entity.RolePermissionId;

import java.util.List;
import java.util.UUID;

public interface RolePermissionRepository extends JpaRepository<RolePermission, RolePermissionId> {

	@Query("""
			SELECT p FROM Permission p
			JOIN RolePermission rp ON rp.id.permissionId = p.id
			JOIN Role r ON r.id = rp.id.roleId
			WHERE rp.id.roleId = :roleId AND r.organization.id = :organizationId
			ORDER BY p.module, p.action
			""")
	List<Permission> findPermissionsByRoleIdAndOrganizationId(
			@Param("roleId") UUID roleId,
			@Param("organizationId") UUID organizationId
	);
}
