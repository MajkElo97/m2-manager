package pl.m2manager.permission.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.m2manager.permission.entity.Permission;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PermissionRepository extends JpaRepository<Permission, UUID> {

	Optional<Permission> findByCode(String code);

	List<Permission> findAllByModule(String module);
}
