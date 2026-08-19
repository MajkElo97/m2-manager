package pl.m2manager.employee.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.m2manager.employee.entity.Employee;
import pl.m2manager.employee.entity.EmployeeRole;
import pl.m2manager.employee.entity.EmploymentType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmployeeRepository extends JpaRepository<Employee, UUID> {

	Optional<Employee> findByIdAndOrganizationId(UUID id, UUID organizationId);

	Optional<Employee> findByOrganizationIdAndCode(UUID organizationId, String code);

	@Query("""
			SELECT e FROM Employee e
			WHERE e.organization.id = :organizationId
			  AND (:active IS NULL OR e.active = :active)
			  AND (:position IS NULL OR :position = '' OR e.position = :position)
			  AND (:role IS NULL OR e.role = :role)
			  AND (:employmentType IS NULL OR e.employmentType = :employmentType)
			  AND (
			    :search IS NULL OR :search = '' OR
			    LOWER(e.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR
			    LOWER(e.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR
			    LOWER(e.phone) LIKE LOWER(CONCAT('%', :search, '%')) OR
			    LOWER(e.email) LIKE LOWER(CONCAT('%', :search, '%')) OR
			    LOWER(e.googleEmail) LIKE LOWER(CONCAT('%', :search, '%'))
			  )
			ORDER BY e.lastName ASC NULLS LAST, e.firstName ASC
			""")
	List<Employee> findAllByOrganizationIdAndFilters(
			@Param("organizationId") UUID organizationId,
			@Param("search") String search,
			@Param("position") String position,
			@Param("role") EmployeeRole role,
			@Param("employmentType") EmploymentType employmentType,
			@Param("active") Boolean active
	);
}
