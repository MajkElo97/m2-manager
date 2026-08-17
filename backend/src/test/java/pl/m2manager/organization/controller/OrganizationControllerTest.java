package pl.m2manager.organization.controller;

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
import pl.m2manager.organization.dto.request.UpdateOrganizationRequest;
import pl.m2manager.organization.dto.response.OrganizationResponse;
import pl.m2manager.organization.service.OrganizationService;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrganizationController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class OrganizationControllerTest {

	private static final UUID ORGANIZATION_ID = UUID.fromString("a0000000-0000-4000-8000-000000000001");
	private static final Instant CREATED_AT = Instant.parse("2026-01-01T00:00:00Z");
	private static final Instant UPDATED_AT = Instant.parse("2026-01-02T00:00:00Z");

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private OrganizationService organizationService;

	private OrganizationResponse sampleResponse() {
		return new OrganizationResponse(
				ORGANIZATION_ID,
				"M2 Manager Dev",
				"1234567890",
				"dev@m2manager.local",
				"+48123456789",
				true,
				"Europe/Warsaw",
				CREATED_AT,
				UPDATED_AT
		);
	}

	@Test
	void getCurrentOrganization_returnsOrganizationResponse() throws Exception {
		when(organizationService.getCurrentOrganization()).thenReturn(sampleResponse());

		mockMvc.perform(get("/api/organization"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(ORGANIZATION_ID.toString()))
				.andExpect(jsonPath("$.name").value("M2 Manager Dev"))
				.andExpect(jsonPath("$.nip").value("1234567890"))
				.andExpect(jsonPath("$.email").value("dev@m2manager.local"))
				.andExpect(jsonPath("$.phone").value("+48123456789"))
				.andExpect(jsonPath("$.active").value(true))
				.andExpect(jsonPath("$.timezone").value("Europe/Warsaw"))
				.andExpect(jsonPath("$.createdAt").value(CREATED_AT.toString()))
				.andExpect(jsonPath("$.updatedAt").value(UPDATED_AT.toString()));

		verify(organizationService).getCurrentOrganization();
	}

	@Test
	void updateCurrentOrganization_withValidRequest_returnsUpdatedOrganization() throws Exception {
		OrganizationResponse updatedResponse = new OrganizationResponse(
				ORGANIZATION_ID,
				"Updated Name",
				"9988776655",
				"updated@example.com",
				"+48987654321",
				true,
				"Europe/Berlin",
				CREATED_AT,
				Instant.parse("2026-01-03T00:00:00Z")
		);

		when(organizationService.updateCurrentOrganization(any(UpdateOrganizationRequest.class)))
				.thenReturn(updatedResponse);

		mockMvc.perform(put("/api/organization")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "Updated Name",
								  "nip": "9988776655",
								  "email": "updated@example.com",
								  "phone": "+48987654321",
								  "timezone": "Europe/Berlin"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Updated Name"))
				.andExpect(jsonPath("$.timezone").value("Europe/Berlin"));

		verify(organizationService).updateCurrentOrganization(any(UpdateOrganizationRequest.class));
	}

	@Test
	void updateCurrentOrganization_withInvalidRequest_returnsValidationError() throws Exception {
		mockMvc.perform(put("/api/organization")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "",
								  "email": "not-an-email",
								  "timezone": ""
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.error").value("Bad Request"))
				.andExpect(jsonPath("$.message").value("Validation failed"))
				.andExpect(jsonPath("$.path").value("/api/organization"))
				.andExpect(jsonPath("$.fieldErrors").isArray())
				.andExpect(jsonPath("$.fieldErrors[?(@.field == 'name')]").exists())
				.andExpect(jsonPath("$.fieldErrors[?(@.field == 'email')]").exists())
				.andExpect(jsonPath("$.fieldErrors[?(@.field == 'timezone')]").exists());
	}

	@Test
	void getCurrentOrganization_whenNotFound_returnsNotFoundError() throws Exception {
		when(organizationService.getCurrentOrganization())
				.thenThrow(new ResourceNotFoundException("Organization", ORGANIZATION_ID));

		mockMvc.perform(get("/api/organization"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.error").value("Not Found"))
				.andExpect(jsonPath("$.message").value("Organization not found: " + ORGANIZATION_ID))
				.andExpect(jsonPath("$.path").value("/api/organization"));
	}

	@Test
	void updateCurrentOrganization_whenNotFound_returnsNotFoundError() throws Exception {
		when(organizationService.updateCurrentOrganization(any(UpdateOrganizationRequest.class)))
				.thenThrow(new ResourceNotFoundException("Organization", ORGANIZATION_ID));

		mockMvc.perform(put("/api/organization")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "Updated Name",
								  "timezone": "Europe/Warsaw"
								}
								"""))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.message").value("Organization not found: " + ORGANIZATION_ID))
				.andExpect(jsonPath("$.path").value("/api/organization"));
	}
}
