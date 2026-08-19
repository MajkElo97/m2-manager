package pl.m2manager.supervisor.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pl.m2manager.common.exception.BusinessConflictException;
import pl.m2manager.common.exception.GlobalExceptionHandler;
import pl.m2manager.common.exception.ResourceNotFoundException;
import pl.m2manager.supervisor.dto.response.SupervisorResponse;
import pl.m2manager.supervisor.service.SupervisorService;

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

@WebMvcTest(SupervisorController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class SupervisorControllerTest {

	private static final UUID MANAGER_ID = UUID.fromString("f0000000-0000-4000-8000-000000000001");
	private static final UUID SUPERVISOR_ID = UUID.fromString("f1000000-0000-4000-8000-000000000001");
	private static final Instant CREATED_AT = Instant.parse("2026-01-01T00:00:00Z");
	private static final Instant UPDATED_AT = Instant.parse("2026-01-02T00:00:00Z");

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private SupervisorService supervisorService;

	@Test
	void list_returns200() throws Exception {
		when(supervisorService.getAll(null, null, null)).thenReturn(List.of(sampleResponse()));

		mockMvc.perform(get("/api/supervisors"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].code").value("OP0001"))
				.andExpect(jsonPath("$[0].managerCode").value("ZA0001"));
	}

	@Test
	void list_withFilters_returns200() throws Exception {
		when(supervisorService.getAll(MANAGER_ID, true, "kozera")).thenReturn(List.of(sampleResponse()));

		mockMvc.perform(get("/api/supervisors")
						.param("managerId", MANAGER_ID.toString())
						.param("active", "true")
						.param("search", "kozera"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].code").value("OP0001"));
	}

	@Test
	void getById_returns200() throws Exception {
		when(supervisorService.getById(SUPERVISOR_ID)).thenReturn(sampleResponse());

		mockMvc.perform(get("/api/supervisors/{id}", SUPERVISOR_ID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(SUPERVISOR_ID.toString()));
	}

	@Test
	void create_returns201() throws Exception {
		when(supervisorService.create(any())).thenReturn(sampleResponse());

		mockMvc.perform(post("/api/supervisors")
						.contentType(MediaType.APPLICATION_JSON)
						.content(validCreatePayload()))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.code").value("OP0001"));
	}

	@Test
	void update_returns200() throws Exception {
		when(supervisorService.update(eq(SUPERVISOR_ID), any())).thenReturn(sampleResponse());

		mockMvc.perform(put("/api/supervisors/{id}", SUPERVISOR_ID)
						.contentType(MediaType.APPLICATION_JSON)
						.content(validUpdatePayload()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value("OP0001"));
	}

	@Test
	void deactivate_returns204() throws Exception {
		mockMvc.perform(delete("/api/supervisors/{id}", SUPERVISOR_ID))
				.andExpect(status().isNoContent());

		verify(supervisorService).deactivate(SUPERVISOR_ID);
	}

	@Test
	void create_withInvalidRequest_returns400() throws Exception {
		mockMvc.perform(post("/api/supervisors")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "managerId": "%s",
								  "code": "",
								  "firstName": "",
								  "lastName": ""
								}
								""".formatted(MANAGER_ID)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400));
	}

	@Test
	void create_withDuplicateCode_returns409() throws Exception {
		when(supervisorService.create(any()))
				.thenThrow(new BusinessConflictException("Supervisor code already exists in organization"));

		mockMvc.perform(post("/api/supervisors")
						.contentType(MediaType.APPLICATION_JSON)
						.content(validCreatePayload()))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(409));
	}

	@Test
	void getById_whenNotFound_returns404() throws Exception {
		when(supervisorService.getById(SUPERVISOR_ID))
				.thenThrow(new ResourceNotFoundException("Supervisor", SUPERVISOR_ID));

		mockMvc.perform(get("/api/supervisors/{id}", SUPERVISOR_ID))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404));
	}

	private SupervisorResponse sampleResponse() {
		return new SupervisorResponse(
				SUPERVISOR_ID,
				MANAGER_ID,
				"ZA0001",
				"Kozera Nieruchomości",
				"OP0001",
				"Magdalena",
				"Kozera",
				"509451780",
				"czynsze@kozeranieruchomosci.pl",
				null,
				true,
				CREATED_AT,
				UPDATED_AT
		);
	}

	private String validCreatePayload() {
		return """
				{
				  "managerId": "%s",
				  "code": "OP0001",
				  "firstName": "Magdalena",
				  "lastName": "Kozera",
				  "phone": "509451780",
				  "email": "czynsze@kozeranieruchomosci.pl"
				}
				""".formatted(MANAGER_ID);
	}

	private String validUpdatePayload() {
		return """
				{
				  "managerId": "%s",
				  "code": "OP0001",
				  "firstName": "Magdalena",
				  "lastName": "Kozera",
				  "phone": "509451780",
				  "email": "czynsze@kozeranieruchomosci.pl",
				  "active": true
				}
				""".formatted(MANAGER_ID);
	}
}
