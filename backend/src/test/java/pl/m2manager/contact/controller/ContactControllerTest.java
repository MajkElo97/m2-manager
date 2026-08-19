package pl.m2manager.contact.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pl.m2manager.common.exception.GlobalExceptionHandler;
import pl.m2manager.common.exception.ResourceNotFoundException;
import pl.m2manager.contact.dto.response.ContactResponse;
import pl.m2manager.contact.service.ContactService;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ContactController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class ContactControllerTest {

	private static final UUID BUILDING_ID = UUID.fromString("d0000000-0000-4000-8000-000000000001");
	private static final UUID CONTACT_ID = UUID.fromString("f3000000-0000-4000-8000-000000000001");
	private static final Instant CREATED_AT = Instant.parse("2026-01-01T00:00:00Z");
	private static final Instant UPDATED_AT = Instant.parse("2026-01-02T00:00:00Z");

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ContactService contactService;

	@Test
	void list_returns200() throws Exception {
		when(contactService.getAll(null)).thenReturn(List.of(sampleResponse()));

		mockMvc.perform(get("/api/contacts"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].firstName").value("Michał"))
				.andExpect(jsonPath("$[0].buildingCode").value("BLD001"));
	}

	@Test
	void list_withBuildingId_returns200() throws Exception {
		when(contactService.getAll(BUILDING_ID)).thenReturn(List.of(sampleResponse()));

		mockMvc.perform(get("/api/contacts").param("buildingId", BUILDING_ID.toString()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].buildingId").value(BUILDING_ID.toString()));
	}

	@Test
	void getById_returns200() throws Exception {
		when(contactService.getById(CONTACT_ID)).thenReturn(sampleResponse());

		mockMvc.perform(get("/api/contacts/{id}", CONTACT_ID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(CONTACT_ID.toString()));
	}

	@Test
	void create_returns201() throws Exception {
		when(contactService.create(any())).thenReturn(sampleResponse());

		mockMvc.perform(post("/api/contacts")
						.contentType(MediaType.APPLICATION_JSON)
						.content(validCreatePayload()))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.firstName").value("Michał"));
	}

	@Test
	void update_returns200() throws Exception {
		when(contactService.update(eq(CONTACT_ID), any())).thenReturn(sampleResponse());

		mockMvc.perform(put("/api/contacts/{id}", CONTACT_ID)
						.contentType(MediaType.APPLICATION_JSON)
						.content(validUpdatePayload()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.firstName").value("Michał"));
	}

	@Test
	void deactivate_returns204() throws Exception {
		mockMvc.perform(delete("/api/contacts/{id}", CONTACT_ID))
				.andExpect(status().isNoContent());

		verify(contactService).deactivate(CONTACT_ID);
	}

	@Test
	void getById_whenNotFound_returns404() throws Exception {
		when(contactService.getById(CONTACT_ID))
				.thenThrow(new ResourceNotFoundException("Contact", CONTACT_ID));

		mockMvc.perform(get("/api/contacts/{id}", CONTACT_ID))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404));
	}

	private ContactResponse sampleResponse() {
		return new ContactResponse(
				CONTACT_ID,
				BUILDING_ID,
				"BLD001",
				"Building One",
				"Michał",
				"Ociepka",
				"Członek zarządu",
				"516154328",
				"m.ocpieka97@gmail.com",
				null,
				true,
				CREATED_AT,
				UPDATED_AT
		);
	}

	private String validCreatePayload() {
		return """
				{
				  "buildingId": "%s",
				  "firstName": "Michał",
				  "lastName": "Ociepka",
				  "functionTitle": "Członek zarządu",
				  "phone": "516154328",
				  "email": "m.ocpieka97@gmail.com"
				}
				""".formatted(BUILDING_ID);
	}

	private String validUpdatePayload() {
		return """
				{
				  "buildingId": "%s",
				  "firstName": "Michał",
				  "lastName": "Ociepka",
				  "functionTitle": "Członek zarządu",
				  "phone": "516154328",
				  "email": "m.ocpieka97@gmail.com",
				  "active": true
				}
				""".formatted(BUILDING_ID);
	}
}
