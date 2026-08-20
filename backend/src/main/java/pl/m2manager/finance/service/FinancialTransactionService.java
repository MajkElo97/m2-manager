package pl.m2manager.finance.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.m2manager.building.entity.Building;
import pl.m2manager.common.exception.BusinessConflictException;
import pl.m2manager.common.exception.ResourceNotFoundException;
import pl.m2manager.employee.entity.Employee;
import pl.m2manager.finance.dto.request.CreateFinancialTransactionRequest;
import pl.m2manager.finance.dto.request.UpdateFinancialTransactionRequest;
import pl.m2manager.finance.dto.response.FinancialTransactionResponse;
import pl.m2manager.finance.entity.FinancialCategory;
import pl.m2manager.finance.entity.FinancialTransaction;
import pl.m2manager.finance.entity.PaymentStatus;
import pl.m2manager.finance.entity.TransactionStatus;
import pl.m2manager.finance.entity.TransactionType;
import pl.m2manager.finance.mapper.FinancialTransactionMapper;
import pl.m2manager.building.repository.BuildingRepository;
import pl.m2manager.finance.repository.FinancialCategoryRepository;
import pl.m2manager.finance.repository.FinancialTransactionRepository;
import pl.m2manager.fleet.entity.Vehicle;
import pl.m2manager.inventory.entity.Chemical;
import pl.m2manager.inventory.entity.Equipment;
import pl.m2manager.tenant.TenantContext;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class FinancialTransactionService {

	private final FinancialTransactionRepository transactionRepository;
	private final FinancialCategoryRepository categoryRepository;
	private final BuildingRepository buildingRepository;
	private final FinanceReferenceResolver referenceResolver;
	private final TenantContext tenantContext;
	private final FinancialTransactionMapper transactionMapper;

	public FinancialTransactionService(
			FinancialTransactionRepository transactionRepository,
			FinancialCategoryRepository categoryRepository,
			BuildingRepository buildingRepository,
			FinanceReferenceResolver referenceResolver,
			TenantContext tenantContext,
			FinancialTransactionMapper transactionMapper
	) {
		this.transactionRepository = transactionRepository;
		this.categoryRepository = categoryRepository;
		this.buildingRepository = buildingRepository;
		this.referenceResolver = referenceResolver;
		this.tenantContext = tenantContext;
		this.transactionMapper = transactionMapper;
	}

	public List<FinancialTransactionResponse> getAll(
			String search,
			TransactionType type,
			UUID categoryId,
			UUID buildingId,
			UUID employeeId,
			UUID vehicleId,
			PaymentStatus paymentStatus,
			TransactionStatus status,
			LocalDate dateFrom,
			LocalDate dateTo
	) {
		UUID organizationId = tenantContext.getCurrentOrganizationId();
		List<FinancialTransaction> transactions = transactionRepository.findAllByOrganizationIdAndFilters(
				organizationId,
				normalizeSearch(search),
				type,
				categoryId,
				buildingId,
				employeeId,
				vehicleId,
				paymentStatus,
				status,
				dateFrom,
				dateTo
		);
		Map<UUID, FinancialCategory> categories = loadCategories(organizationId, transactions);
		Map<UUID, Building> buildings = loadBuildings(organizationId, transactions);
		return transactions.stream()
				.map(transaction -> transactionMapper.toResponse(
						transaction,
						categories.get(transaction.getCategoryId()),
						buildings.get(transaction.getBuildingId()),
						null,
						null,
						null,
						null
				))
				.toList();
	}

	public FinancialTransactionResponse getById(UUID transactionId) {
		return toDetailedResponse(requireTransactionInCurrentOrganization(transactionId));
	}

	@Transactional
	public FinancialTransactionResponse create(CreateFinancialTransactionRequest request) {
		UUID organizationId = tenantContext.getCurrentOrganizationId();
		assertUniqueCode(organizationId, request.code(), null);

		ResolvedReferences refs = resolveReferences(organizationId, request);
		FinanceAmountCalculator.Amounts amounts = FinanceAmountCalculator.calculate(request.netAmount(), request.vatRate());

		FinancialTransaction transaction = new FinancialTransaction();
		transaction.setOrganizationId(organizationId);
		applyFields(transaction, request, refs, amounts);
		transaction.setStatus(TransactionStatus.ACTIVE);

		try {
			return toDetailedResponse(transactionRepository.saveAndFlush(transaction));
		}
		catch (DataIntegrityViolationException ex) {
			throw new BusinessConflictException("Transaction code already exists in organization");
		}
	}

	@Transactional
	public FinancialTransactionResponse update(UUID transactionId, UpdateFinancialTransactionRequest request) {
		UUID organizationId = tenantContext.getCurrentOrganizationId();
		FinancialTransaction transaction = requireTransactionInCurrentOrganization(transactionId);
		assertUniqueCode(organizationId, request.code(), transactionId);

		ResolvedReferences refs = resolveReferences(organizationId, request);
		FinanceAmountCalculator.Amounts amounts = FinanceAmountCalculator.calculate(request.netAmount(), request.vatRate());
		applyFields(transaction, request, refs, amounts);

		try {
			return toDetailedResponse(transactionRepository.save(transaction));
		}
		catch (DataIntegrityViolationException ex) {
			throw new BusinessConflictException("Transaction code already exists in organization");
		}
	}

	@Transactional
	public void cancel(UUID transactionId) {
		FinancialTransaction transaction = requireTransactionInCurrentOrganization(transactionId);
		if (transaction.getStatus() == TransactionStatus.CANCELLED) {
			return;
		}
		transaction.setStatus(TransactionStatus.CANCELLED);
		transactionRepository.save(transaction);
	}

	private void applyFields(
			FinancialTransaction transaction,
			CreateFinancialTransactionRequest request,
			ResolvedReferences refs,
			FinanceAmountCalculator.Amounts amounts
	) {
		transaction.setCode(request.code());
		transaction.setTransactionDate(request.transactionDate());
		transaction.setType(request.type());
		transaction.setNetAmount(amounts.netAmount());
		transaction.setVatRate(amounts.vatRate());
		transaction.setVatAmount(amounts.vatAmount());
		transaction.setGrossAmount(amounts.grossAmount());
		transaction.setCategoryId(refs.category().getId());
		transaction.setContractorName(request.contractorName());
		transaction.setContractorNip(request.contractorNip());
		transaction.setBuildingId(refs.building() != null ? refs.building().getId() : null);
		transaction.setEmployeeId(refs.employee() != null ? refs.employee().getId() : null);
		transaction.setVehicleId(refs.vehicle() != null ? refs.vehicle().getId() : null);
		transaction.setEquipmentId(refs.equipment() != null ? refs.equipment().getId() : null);
		transaction.setChemicalId(refs.chemical() != null ? refs.chemical().getId() : null);
		transaction.setDescription(request.description());
		transaction.setDocumentNumber(request.documentNumber());
		transaction.setDueDate(request.dueDate());
		transaction.setPaymentDate(request.paymentDate());
		transaction.setPaymentStatus(request.paymentStatus());
		transaction.setNotes(request.notes());
	}

	private void applyFields(
			FinancialTransaction transaction,
			UpdateFinancialTransactionRequest request,
			ResolvedReferences refs,
			FinanceAmountCalculator.Amounts amounts
	) {
		applyFields(transaction, new CreateFinancialTransactionRequest(
				request.code(),
				request.transactionDate(),
				request.type(),
				request.netAmount(),
				request.vatRate(),
				request.categoryId(),
				request.contractorName(),
				request.contractorNip(),
				request.buildingId(),
				request.employeeId(),
				request.vehicleId(),
				request.equipmentId(),
				request.chemicalId(),
				request.description(),
				request.documentNumber(),
				request.dueDate(),
				request.paymentDate(),
				request.paymentStatus(),
				request.notes()
		), refs, amounts);
	}

	private ResolvedReferences resolveReferences(UUID organizationId, CreateFinancialTransactionRequest request) {
		return new ResolvedReferences(
				referenceResolver.resolveCategory(organizationId, request.categoryId(), request.type()),
				referenceResolver.resolveBuilding(organizationId, request.buildingId()),
				referenceResolver.resolveEmployee(organizationId, request.employeeId()),
				referenceResolver.resolveVehicle(organizationId, request.vehicleId()),
				referenceResolver.resolveEquipment(organizationId, request.equipmentId()),
				referenceResolver.resolveChemical(organizationId, request.chemicalId())
		);
	}

	private ResolvedReferences resolveReferences(UUID organizationId, UpdateFinancialTransactionRequest request) {
		return resolveReferences(organizationId, new CreateFinancialTransactionRequest(
				request.code(),
				request.transactionDate(),
				request.type(),
				request.netAmount(),
				request.vatRate(),
				request.categoryId(),
				request.contractorName(),
				request.contractorNip(),
				request.buildingId(),
				request.employeeId(),
				request.vehicleId(),
				request.equipmentId(),
				request.chemicalId(),
				request.description(),
				request.documentNumber(),
				request.dueDate(),
				request.paymentDate(),
				request.paymentStatus(),
				request.notes()
		));
	}

	private FinancialTransactionResponse toDetailedResponse(FinancialTransaction transaction) {
		UUID organizationId = tenantContext.getCurrentOrganizationId();
		FinancialCategory category = categoryRepository.findByIdAndOrganizationId(transaction.getCategoryId(), organizationId).orElse(null);
		Building building = referenceResolver.resolveBuilding(organizationId, transaction.getBuildingId());
		Employee employee = referenceResolver.resolveEmployee(organizationId, transaction.getEmployeeId());
		Vehicle vehicle = referenceResolver.resolveVehicle(organizationId, transaction.getVehicleId());
		Equipment equipment = referenceResolver.resolveEquipment(organizationId, transaction.getEquipmentId());
		Chemical chemical = referenceResolver.resolveChemical(organizationId, transaction.getChemicalId());
		return transactionMapper.toResponse(transaction, category, building, employee, vehicle, equipment, chemical);
	}

	private Map<UUID, Building> loadBuildings(UUID organizationId, List<FinancialTransaction> transactions) {
		List<UUID> buildingIds = transactions.stream()
				.map(FinancialTransaction::getBuildingId)
				.filter(id -> id != null)
				.distinct()
				.toList();
		if (buildingIds.isEmpty()) {
			return Map.of();
		}
		Map<UUID, Building> buildings = new HashMap<>();
		buildingRepository.findAllByIdInAndOrganizationId(buildingIds, organizationId)
				.forEach(building -> buildings.put(building.getId(), building));
		return buildings;
	}

	private Map<UUID, FinancialCategory> loadCategories(UUID organizationId, List<FinancialTransaction> transactions) {
		List<UUID> categoryIds = transactions.stream()
				.map(FinancialTransaction::getCategoryId)
				.distinct()
				.toList();
		if (categoryIds.isEmpty()) {
			return Map.of();
		}
		Map<UUID, FinancialCategory> categories = new HashMap<>();
		categoryRepository.findAllById(categoryIds).stream()
				.filter(category -> category.getOrganizationId().equals(organizationId))
				.forEach(category -> categories.put(category.getId(), category));
		return categories;
	}

	private FinancialTransaction requireTransactionInCurrentOrganization(UUID transactionId) {
		UUID organizationId = tenantContext.getCurrentOrganizationId();
		return transactionRepository.findByIdAndOrganizationId(transactionId, organizationId)
				.orElseThrow(() -> new ResourceNotFoundException("FinancialTransaction", transactionId));
	}

	private void assertUniqueCode(UUID organizationId, String code, UUID excludeTransactionId) {
		transactionRepository.findByOrganizationIdAndCode(organizationId, code).ifPresent(existing -> {
			if (excludeTransactionId == null || !existing.getId().equals(excludeTransactionId)) {
				throw new BusinessConflictException("Transaction code already exists in organization");
			}
		});
	}

	private String normalizeSearch(String search) {
		if (search == null) {
			return null;
		}
		String trimmed = search.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	private record ResolvedReferences(
			FinancialCategory category,
			Building building,
			Employee employee,
			Vehicle vehicle,
			Equipment equipment,
			Chemical chemical
	) {
	}
}
