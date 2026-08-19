package pl.m2manager.fleet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.m2manager.fleet.entity.Vehicle;
import pl.m2manager.fleet.entity.VehicleStatus;
import pl.m2manager.fleet.entity.VehicleType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {

	Optional<Vehicle> findByIdAndOrganizationId(UUID id, UUID organizationId);

	Optional<Vehicle> findByOrganizationIdAndCode(UUID organizationId, String code);

	Optional<Vehicle> findByOrganizationIdAndRegistrationNumber(UUID organizationId, String registrationNumber);

	@Query("""
			SELECT v FROM Vehicle v
			WHERE v.organizationId = :organizationId
			  AND (:status IS NULL OR v.status = :status)
			  AND (:employeeId IS NULL OR v.employeeId = :employeeId)
			  AND (:vehicleType IS NULL OR v.vehicleType = :vehicleType)
			  AND (
			    :search IS NULL OR :search = '' OR
			    LOWER(v.code) LIKE LOWER(CONCAT('%', :search, '%')) OR
			    LOWER(v.registrationNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR
			    LOWER(v.make) LIKE LOWER(CONCAT('%', :search, '%')) OR
			    LOWER(v.model) LIKE LOWER(CONCAT('%', :search, '%')) OR
			    LOWER(v.vin) LIKE LOWER(CONCAT('%', :search, '%'))
			  )
			ORDER BY v.code ASC
			""")
	List<Vehicle> findAllByOrganizationIdAndFilters(
			@Param("organizationId") UUID organizationId,
			@Param("search") String search,
			@Param("status") VehicleStatus status,
			@Param("employeeId") UUID employeeId,
			@Param("vehicleType") VehicleType vehicleType
	);
}
