package pl.m2manager.contact.mapper;

import org.springframework.stereotype.Component;
import pl.m2manager.building.entity.Building;
import pl.m2manager.building.repository.BuildingRepository;
import pl.m2manager.contact.dto.request.CreateContactRequest;
import pl.m2manager.contact.dto.request.UpdateContactRequest;
import pl.m2manager.contact.dto.response.ContactResponse;
import pl.m2manager.contact.entity.Contact;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ContactMapper {

	private final BuildingRepository buildingRepository;

	public ContactMapper(BuildingRepository buildingRepository) {
		this.buildingRepository = buildingRepository;
	}

	public ContactResponse toResponse(Contact contact, UUID organizationId) {
		Building building = buildingRepository.findByIdAndOrganizationId(contact.getBuildingId(), organizationId).orElse(null);
		return toResponse(contact, building);
	}

	public List<ContactResponse> toResponseList(List<Contact> contacts, UUID organizationId) {
		Map<UUID, Building> buildingsById = buildingRepository
				.findAllById(contacts.stream().map(Contact::getBuildingId).collect(Collectors.toSet())).stream()
				.filter(building -> building.getOrganization().getId().equals(organizationId))
				.collect(Collectors.toMap(Building::getId, Function.identity()));

		return contacts.stream()
				.map(contact -> toResponse(contact, buildingsById.get(contact.getBuildingId())))
				.toList();
	}

	public void applyCreate(Contact contact, CreateContactRequest request) {
		contact.setBuildingId(request.buildingId());
		contact.setFirstName(request.firstName());
		contact.setLastName(request.lastName());
		contact.setFunctionTitle(request.functionTitle());
		contact.setPhone(request.phone());
		contact.setEmail(request.email());
		contact.setNotes(request.notes());
		contact.setActive(true);
	}

	public void applyUpdate(Contact contact, UpdateContactRequest request) {
		contact.setBuildingId(request.buildingId());
		contact.setFirstName(request.firstName());
		contact.setLastName(request.lastName());
		contact.setFunctionTitle(request.functionTitle());
		contact.setPhone(request.phone());
		contact.setEmail(request.email());
		contact.setNotes(request.notes());
		contact.setActive(request.active());
	}

	private ContactResponse toResponse(Contact contact, Building building) {
		return new ContactResponse(
				contact.getId(),
				contact.getBuildingId(),
				building != null ? building.getCode() : null,
				building != null ? building.getName() : null,
				contact.getFirstName(),
				contact.getLastName(),
				contact.getFunctionTitle(),
				contact.getPhone(),
				contact.getEmail(),
				contact.getNotes(),
				contact.isActive(),
				contact.getCreatedAt(),
				contact.getUpdatedAt()
		);
	}
}
