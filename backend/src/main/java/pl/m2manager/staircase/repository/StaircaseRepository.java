package pl.m2manager.staircase.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.m2manager.staircase.entity.Staircase;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StaircaseRepository extends JpaRepository<Staircase, UUID> {

	Optional<Staircase> findByIdAndOrganizationId(UUID id, UUID organizationId);

	List<Staircase> findAllByOrganizationIdAndBuildingIdOrderByDesignationAsc(UUID organizationId, UUID buildingId);

	List<Staircase> findAllByOrganizationIdOrderByCodeAsc(UUID organizationId);

	Optional<Staircase> findByOrganizationIdAndCode(UUID organizationId, String code);

	Optional<Staircase> findByBuildingIdAndDesignation(UUID buildingId, String designation);

	long countByOrganizationIdAndBuildingId(UUID organizationId, UUID buildingId);
}
