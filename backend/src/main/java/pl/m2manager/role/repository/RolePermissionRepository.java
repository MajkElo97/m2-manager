package pl.m2manager.role.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.m2manager.role.entity.RolePermission;
import pl.m2manager.role.entity.RolePermissionId;

public interface RolePermissionRepository extends JpaRepository<RolePermission, RolePermissionId> {
}
