package pl.m2manager.finance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.m2manager.finance.entity.FinancialCategory;
import pl.m2manager.finance.entity.TransactionType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FinancialCategoryRepository extends JpaRepository<FinancialCategory, UUID> {

	Optional<FinancialCategory> findByIdAndOrganizationId(UUID id, UUID organizationId);

	Optional<FinancialCategory> findByOrganizationIdAndCode(UUID organizationId, String code);

	@Query("""
			SELECT c FROM FinancialCategory c
			WHERE c.organizationId = :organizationId
			  AND (:active IS NULL OR c.active = :active)
			  AND (:type IS NULL OR c.type = :type)
			  AND (
			    :search IS NULL OR :search = '' OR
			    LOWER(c.code) LIKE LOWER(CONCAT('%', :search, '%')) OR
			    LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%'))
			  )
			ORDER BY c.type ASC, c.name ASC
			""")
	List<FinancialCategory> findAllByOrganizationIdAndFilters(
			@Param("organizationId") UUID organizationId,
			@Param("search") String search,
			@Param("type") TransactionType type,
			@Param("active") Boolean active
	);
}
