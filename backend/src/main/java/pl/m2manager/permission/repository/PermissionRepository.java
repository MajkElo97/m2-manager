package pl.m2manager.permission.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.m2manager.permission.entity.Permission;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PermissionRepository extends JpaRepository<Permission, UUID> {

	Optional<Permission> findByCode(String code);

	List<Permission> findAllByModule(String module);

	List<Permission> findAllByCodeIn(Collection<String> codes);

	@Query("SELECT p.code FROM Permission p ORDER BY p.code")
	List<String> findAllPermissionCodes();

	@Query("""
			SELECT p FROM Permission p
			WHERE p.module IN :modules AND p.action IN :actions
			ORDER BY p.code
			""")
	List<Permission> findByModulesAndActions(
			@Param("modules") Collection<String> modules,
			@Param("actions") Collection<String> actions
	);

	@Query("""
			SELECT p FROM Permission p
			WHERE p.module = :module AND p.action = :action
			""")
	List<Permission> findByModuleAndAction(
			@Param("module") String module,
			@Param("action") String action
	);
}
