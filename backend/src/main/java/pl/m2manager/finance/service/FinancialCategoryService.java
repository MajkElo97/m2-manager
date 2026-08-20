package pl.m2manager.finance.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.m2manager.common.exception.BusinessConflictException;
import pl.m2manager.common.exception.ResourceNotFoundException;
import pl.m2manager.finance.dto.request.CreateFinancialCategoryRequest;
import pl.m2manager.finance.dto.request.UpdateFinancialCategoryRequest;
import pl.m2manager.finance.dto.response.FinancialCategoryResponse;
import pl.m2manager.finance.entity.FinancialCategory;
import pl.m2manager.finance.entity.TransactionType;
import pl.m2manager.finance.mapper.FinancialCategoryMapper;
import pl.m2manager.finance.repository.FinancialCategoryRepository;
import pl.m2manager.tenant.TenantContext;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class FinancialCategoryService {

	private final FinancialCategoryRepository categoryRepository;
	private final TenantContext tenantContext;
	private final FinancialCategoryMapper categoryMapper;

	public FinancialCategoryService(
			FinancialCategoryRepository categoryRepository,
			TenantContext tenantContext,
			FinancialCategoryMapper categoryMapper
	) {
		this.categoryRepository = categoryRepository;
		this.tenantContext = tenantContext;
		this.categoryMapper = categoryMapper;
	}

	public List<FinancialCategoryResponse> getAll(String search, TransactionType type, Boolean active) {
		UUID organizationId = tenantContext.getCurrentOrganizationId();
		return categoryRepository.findAllByOrganizationIdAndFilters(
				organizationId,
				normalizeSearch(search),
				type,
				active
		).stream()
				.map(categoryMapper::toResponse)
				.toList();
	}

	public FinancialCategoryResponse getById(UUID categoryId) {
		return categoryMapper.toResponse(requireCategoryInCurrentOrganization(categoryId));
	}

	@Transactional
	public FinancialCategoryResponse create(CreateFinancialCategoryRequest request) {
		UUID organizationId = tenantContext.getCurrentOrganizationId();
		assertUniqueCode(organizationId, request.code(), null);

		FinancialCategory category = new FinancialCategory();
		category.setOrganizationId(organizationId);
		categoryMapper.applyCreate(category, request);

		try {
			return categoryMapper.toResponse(categoryRepository.saveAndFlush(category));
		}
		catch (DataIntegrityViolationException ex) {
			throw new BusinessConflictException("Category code already exists in organization");
		}
	}

	@Transactional
	public FinancialCategoryResponse update(UUID categoryId, UpdateFinancialCategoryRequest request) {
		UUID organizationId = tenantContext.getCurrentOrganizationId();
		FinancialCategory category = requireCategoryInCurrentOrganization(categoryId);
		assertUniqueCode(organizationId, request.code(), categoryId);
		categoryMapper.applyUpdate(category, request);

		try {
			return categoryMapper.toResponse(categoryRepository.save(category));
		}
		catch (DataIntegrityViolationException ex) {
			throw new BusinessConflictException("Category code already exists in organization");
		}
	}

	@Transactional
	public void deactivate(UUID categoryId) {
		FinancialCategory category = requireCategoryInCurrentOrganization(categoryId);
		if (!category.isActive()) {
			return;
		}
		category.setActive(false);
		categoryRepository.save(category);
	}

	private FinancialCategory requireCategoryInCurrentOrganization(UUID categoryId) {
		UUID organizationId = tenantContext.getCurrentOrganizationId();
		return categoryRepository.findByIdAndOrganizationId(categoryId, organizationId)
				.orElseThrow(() -> new ResourceNotFoundException("FinancialCategory", categoryId));
	}

	private void assertUniqueCode(UUID organizationId, String code, UUID excludeCategoryId) {
		categoryRepository.findByOrganizationIdAndCode(organizationId, code).ifPresent(existing -> {
			if (excludeCategoryId == null || !existing.getId().equals(excludeCategoryId)) {
				throw new BusinessConflictException("Category code already exists in organization");
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
}
