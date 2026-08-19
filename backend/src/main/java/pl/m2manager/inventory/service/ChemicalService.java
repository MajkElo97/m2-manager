package pl.m2manager.inventory.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.m2manager.common.exception.BusinessConflictException;
import pl.m2manager.common.exception.ResourceNotFoundException;
import pl.m2manager.inventory.dto.request.CreateChemicalRequest;
import pl.m2manager.inventory.dto.request.UpdateChemicalRequest;
import pl.m2manager.inventory.dto.response.ChemicalResponse;
import pl.m2manager.inventory.entity.Chemical;
import pl.m2manager.inventory.mapper.ChemicalMapper;
import pl.m2manager.inventory.repository.ChemicalRepository;
import pl.m2manager.tenant.TenantContext;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ChemicalService {

	private final ChemicalRepository chemicalRepository;
	private final TenantContext tenantContext;
	private final ChemicalMapper chemicalMapper;

	public ChemicalService(
			ChemicalRepository chemicalRepository,
			TenantContext tenantContext,
			ChemicalMapper chemicalMapper
	) {
		this.chemicalRepository = chemicalRepository;
		this.tenantContext = tenantContext;
		this.chemicalMapper = chemicalMapper;
	}

	public List<ChemicalResponse> getAll(String search, String category, Boolean active, Boolean lowStock) {
		UUID organizationId = tenantContext.getCurrentOrganizationId();
		return chemicalRepository.findAllByOrganizationIdAndFilters(
				organizationId,
				normalizeSearch(search),
				normalize(category),
				active,
				lowStock
		).stream()
				.map(chemicalMapper::toResponse)
				.toList();
	}

	public ChemicalResponse getById(UUID chemicalId) {
		return chemicalMapper.toResponse(requireChemicalInCurrentOrganization(chemicalId));
	}

	@Transactional
	public ChemicalResponse create(CreateChemicalRequest request) {
		UUID organizationId = tenantContext.getCurrentOrganizationId();
		assertUniqueCode(organizationId, request.code(), null);

		Chemical chemical = new Chemical();
		chemical.setOrganizationId(organizationId);
		chemicalMapper.applyCreate(chemical, request);

		try {
			return chemicalMapper.toResponse(chemicalRepository.saveAndFlush(chemical));
		}
		catch (DataIntegrityViolationException ex) {
			throw new BusinessConflictException("Chemical code already exists in organization");
		}
	}

	@Transactional
	public ChemicalResponse update(UUID chemicalId, UpdateChemicalRequest request) {
		UUID organizationId = tenantContext.getCurrentOrganizationId();
		Chemical chemical = requireChemicalInCurrentOrganization(chemicalId);
		assertUniqueCode(organizationId, request.code(), chemicalId);
		chemicalMapper.applyUpdate(chemical, request);

		try {
			return chemicalMapper.toResponse(chemicalRepository.save(chemical));
		}
		catch (DataIntegrityViolationException ex) {
			throw new BusinessConflictException("Chemical code already exists in organization");
		}
	}

	@Transactional
	public void deactivate(UUID chemicalId) {
		Chemical chemical = requireChemicalInCurrentOrganization(chemicalId);
		if (!chemical.isActive()) {
			return;
		}
		chemical.setActive(false);
		chemicalRepository.save(chemical);
	}

	private Chemical requireChemicalInCurrentOrganization(UUID chemicalId) {
		UUID organizationId = tenantContext.getCurrentOrganizationId();
		return chemicalRepository.findByIdAndOrganizationId(chemicalId, organizationId)
				.orElseThrow(() -> new ResourceNotFoundException("Chemical", chemicalId));
	}

	private void assertUniqueCode(UUID organizationId, String code, UUID excludeChemicalId) {
		chemicalRepository.findByOrganizationIdAndCode(organizationId, code).ifPresent(existing -> {
			if (excludeChemicalId == null || !existing.getId().equals(excludeChemicalId)) {
				throw new BusinessConflictException("Chemical code already exists in organization");
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

	private String normalize(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}
}
