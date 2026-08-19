package pl.m2manager.contact.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.m2manager.building.repository.BuildingRepository;
import pl.m2manager.common.exception.ResourceNotFoundException;
import pl.m2manager.contact.dto.request.CreateContactRequest;
import pl.m2manager.contact.dto.request.UpdateContactRequest;
import pl.m2manager.contact.dto.response.ContactResponse;
import pl.m2manager.contact.entity.Contact;
import pl.m2manager.contact.mapper.ContactMapper;
import pl.m2manager.contact.repository.ContactRepository;
import pl.m2manager.tenant.TenantContext;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ContactService {

	private final ContactRepository contactRepository;
	private final BuildingRepository buildingRepository;
	private final TenantContext tenantContext;
	private final ContactMapper contactMapper;

	public ContactService(
			ContactRepository contactRepository,
			BuildingRepository buildingRepository,
			TenantContext tenantContext,
			ContactMapper contactMapper
	) {
		this.contactRepository = contactRepository;
		this.buildingRepository = buildingRepository;
		this.tenantContext = tenantContext;
		this.contactMapper = contactMapper;
	}

	public List<ContactResponse> getAll(UUID buildingId) {
		UUID organizationId = tenantContext.getCurrentOrganizationId();
		if (buildingId != null) {
			requireBuildingInCurrentOrganization(buildingId, organizationId);
		}
		List<Contact> contacts = contactRepository.findAllByOrganizationIdAndBuildingId(organizationId, buildingId);
		return contactMapper.toResponseList(contacts, organizationId);
	}

	public ContactResponse getById(UUID contactId) {
		UUID organizationId = tenantContext.getCurrentOrganizationId();
		return contactMapper.toResponse(requireContactInCurrentOrganization(contactId), organizationId);
	}

	@Transactional
	public ContactResponse create(CreateContactRequest request) {
		UUID organizationId = tenantContext.getCurrentOrganizationId();
		requireBuildingInCurrentOrganization(request.buildingId(), organizationId);

		Contact contact = new Contact();
		contact.setOrganizationId(organizationId);
		contactMapper.applyCreate(contact, request);

		return contactMapper.toResponse(contactRepository.saveAndFlush(contact), organizationId);
	}

	@Transactional
	public ContactResponse update(UUID contactId, UpdateContactRequest request) {
		UUID organizationId = tenantContext.getCurrentOrganizationId();
		Contact contact = requireContactInCurrentOrganization(contactId);
		requireBuildingInCurrentOrganization(request.buildingId(), organizationId);
		contactMapper.applyUpdate(contact, request);

		return contactMapper.toResponse(contactRepository.save(contact), organizationId);
	}

	@Transactional
	public void deactivate(UUID contactId) {
		Contact contact = requireContactInCurrentOrganization(contactId);
		if (!contact.isActive()) {
			return;
		}
		contact.setActive(false);
		contactRepository.save(contact);
	}

	private Contact requireContactInCurrentOrganization(UUID contactId) {
		UUID organizationId = tenantContext.getCurrentOrganizationId();
		return contactRepository.findByIdAndOrganizationId(contactId, organizationId)
				.orElseThrow(() -> new ResourceNotFoundException("Contact", contactId));
	}

	private void requireBuildingInCurrentOrganization(UUID buildingId, UUID organizationId) {
		buildingRepository.findByIdAndOrganizationId(buildingId, organizationId)
				.orElseThrow(() -> new ResourceNotFoundException("Building", buildingId));
	}
}
