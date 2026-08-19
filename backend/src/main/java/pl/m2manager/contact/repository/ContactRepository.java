package pl.m2manager.contact.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.m2manager.contact.entity.Contact;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContactRepository extends JpaRepository<Contact, UUID> {

	Optional<Contact> findByIdAndOrganizationId(UUID id, UUID organizationId);

	@Query("""
			SELECT c FROM Contact c
			WHERE c.organizationId = :organizationId
			  AND (:buildingId IS NULL OR c.buildingId = :buildingId)
			ORDER BY c.lastName ASC NULLS LAST, c.firstName ASC NULLS LAST
			""")
	List<Contact> findAllByOrganizationIdAndBuildingId(
			@Param("organizationId") UUID organizationId,
			@Param("buildingId") UUID buildingId
	);
}
