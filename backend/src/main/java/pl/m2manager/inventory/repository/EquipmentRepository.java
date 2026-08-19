package pl.m2manager.inventory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.m2manager.inventory.entity.Equipment;
import pl.m2manager.inventory.entity.EquipmentCondition;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EquipmentRepository extends JpaRepository<Equipment, UUID> {

	Optional<Equipment> findByIdAndOrganizationId(UUID id, UUID organizationId);

	Optional<Equipment> findByOrganizationIdAndCode(UUID organizationId, String code);

	@Query("""
			SELECT e FROM Equipment e
			WHERE e.organizationId = :organizationId
			  AND (:active IS NULL OR e.active = :active)
			  AND (:category IS NULL OR :category = '' OR LOWER(e.category) = LOWER(:category))
			  AND (:employeeId IS NULL OR e.employeeId = :employeeId)
			  AND (:conditionStatus IS NULL OR e.conditionStatus = :conditionStatus)
			  AND (
			    :search IS NULL OR :search = '' OR
			    LOWER(e.code) LIKE LOWER(CONCAT('%', :search, '%')) OR
			    LOWER(e.name) LIKE LOWER(CONCAT('%', :search, '%')) OR
			    LOWER(e.category) LIKE LOWER(CONCAT('%', :search, '%')) OR
			    LOWER(e.manufacturer) LIKE LOWER(CONCAT('%', :search, '%')) OR
			    LOWER(e.model) LIKE LOWER(CONCAT('%', :search, '%')) OR
			    LOWER(e.serialNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR
			    LOWER(e.location) LIKE LOWER(CONCAT('%', :search, '%'))
			  )
			ORDER BY e.code ASC
			""")
	List<Equipment> findAllByOrganizationIdAndFilters(
			@Param("organizationId") UUID organizationId,
			@Param("search") String search,
			@Param("category") String category,
			@Param("employeeId") UUID employeeId,
			@Param("conditionStatus") EquipmentCondition conditionStatus,
			@Param("active") Boolean active
	);
}
