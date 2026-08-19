package pl.m2manager.contact.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pl.m2manager.contact.dto.request.CreateContactRequest;
import pl.m2manager.contact.dto.request.UpdateContactRequest;
import pl.m2manager.contact.dto.response.ContactResponse;
import pl.m2manager.contact.service.ContactService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/contacts")
public class ContactController {

	private final ContactService contactService;

	public ContactController(ContactService contactService) {
		this.contactService = contactService;
	}

	@GetMapping
	@PreAuthorize("@authorizationService.canListContacts(#buildingId)")
	public List<ContactResponse> list(@RequestParam(required = false) UUID buildingId) {
		return contactService.getAll(buildingId);
	}

	@GetMapping("/{id}")
	@PreAuthorize("@authorizationService.hasPermission('CONTACTS_VIEW')")
	public ContactResponse getById(@PathVariable UUID id) {
		return contactService.getById(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("@authorizationService.hasPermission('CONTACTS_CREATE')")
	public ContactResponse create(@Valid @RequestBody CreateContactRequest request) {
		return contactService.create(request);
	}

	@PutMapping("/{id}")
	@PreAuthorize("@authorizationService.hasPermission('CONTACTS_EDIT')")
	public ContactResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateContactRequest request) {
		return contactService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("@authorizationService.hasPermission('CONTACTS_DELETE')")
	public void deactivate(@PathVariable UUID id) {
		contactService.deactivate(id);
	}
}
