package pl.m2manager.finance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.m2manager.finance.entity.FinancialTransaction;
import pl.m2manager.finance.entity.PaymentStatus;
import pl.m2manager.finance.entity.TransactionStatus;
import pl.m2manager.finance.entity.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FinancialTransactionRepository extends JpaRepository<FinancialTransaction, UUID> {

	Optional<FinancialTransaction> findByIdAndOrganizationId(UUID id, UUID organizationId);

	Optional<FinancialTransaction> findByOrganizationIdAndCode(UUID organizationId, String code);

	@Query("""
			SELECT t FROM FinancialTransaction t
			WHERE t.organizationId = :organizationId
			  AND (:type IS NULL OR t.type = :type)
			  AND (:categoryId IS NULL OR t.categoryId = :categoryId)
			  AND (:buildingId IS NULL OR t.buildingId = :buildingId)
			  AND (:employeeId IS NULL OR t.employeeId = :employeeId)
			  AND (:vehicleId IS NULL OR t.vehicleId = :vehicleId)
			  AND (:paymentStatus IS NULL OR t.paymentStatus = :paymentStatus)
			  AND (:status IS NULL OR t.status = :status)
			  AND (:dateFrom IS NULL OR t.transactionDate >= :dateFrom)
			  AND (:dateTo IS NULL OR t.transactionDate <= :dateTo)
			  AND (
			    :search IS NULL OR :search = '' OR
			    LOWER(t.code) LIKE LOWER(CONCAT('%', :search, '%')) OR
			    LOWER(t.contractorName) LIKE LOWER(CONCAT('%', :search, '%')) OR
			    LOWER(t.contractorNip) LIKE LOWER(CONCAT('%', :search, '%')) OR
			    LOWER(t.documentNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR
			    LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%'))
			  )
			ORDER BY t.transactionDate DESC, t.code DESC
			""")
	List<FinancialTransaction> findAllByOrganizationIdAndFilters(
			@Param("organizationId") UUID organizationId,
			@Param("search") String search,
			@Param("type") TransactionType type,
			@Param("categoryId") UUID categoryId,
			@Param("buildingId") UUID buildingId,
			@Param("employeeId") UUID employeeId,
			@Param("vehicleId") UUID vehicleId,
			@Param("paymentStatus") PaymentStatus paymentStatus,
			@Param("status") TransactionStatus status,
			@Param("dateFrom") LocalDate dateFrom,
			@Param("dateTo") LocalDate dateTo
	);

	@Query("""
			SELECT COALESCE(SUM(t.netAmount), 0) FROM FinancialTransaction t
			WHERE t.organizationId = :organizationId
			  AND t.status = 'ACTIVE'
			  AND t.type = :type
			  AND t.transactionDate >= :dateFrom
			  AND t.transactionDate <= :dateTo
			""")
	BigDecimal sumNetByTypeAndDateRange(
			@Param("organizationId") UUID organizationId,
			@Param("type") TransactionType type,
			@Param("dateFrom") LocalDate dateFrom,
			@Param("dateTo") LocalDate dateTo
	);

	@Query("""
			SELECT COALESCE(SUM(t.grossAmount), 0) FROM FinancialTransaction t
			WHERE t.organizationId = :organizationId
			  AND t.status = 'ACTIVE'
			  AND t.type = :type
			  AND t.transactionDate >= :dateFrom
			  AND t.transactionDate <= :dateTo
			""")
	BigDecimal sumGrossByTypeAndDateRange(
			@Param("organizationId") UUID organizationId,
			@Param("type") TransactionType type,
			@Param("dateFrom") LocalDate dateFrom,
			@Param("dateTo") LocalDate dateTo
	);

	@Query("""
			SELECT COALESCE(SUM(t.grossAmount), 0) FROM FinancialTransaction t
			WHERE t.organizationId = :organizationId
			  AND t.status = 'ACTIVE'
			  AND t.type = :type
			  AND t.paymentStatus = 'TO_PAY'
			""")
	BigDecimal sumOutstandingGross(
			@Param("organizationId") UUID organizationId,
			@Param("type") TransactionType type
	);

	@Query("""
			SELECT COALESCE(SUM(t.grossAmount), 0) FROM FinancialTransaction t
			WHERE t.organizationId = :organizationId
			  AND t.status = 'ACTIVE'
			  AND t.type = :type
			  AND t.paymentStatus = 'TO_PAY'
			  AND t.dueDate IS NOT NULL
			  AND t.dueDate < :today
			""")
	BigDecimal sumOverdueGross(
			@Param("organizationId") UUID organizationId,
			@Param("type") TransactionType type,
			@Param("today") LocalDate today
	);

	long countByOrganizationIdAndBuildingId(UUID organizationId, UUID buildingId);
}
