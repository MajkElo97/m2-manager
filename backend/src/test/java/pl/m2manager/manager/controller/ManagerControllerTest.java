package pl.m2manager.manager.controller;

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
import pl.m2manager.manager.dto.response.ManagerResponse;
import pl.m2manager.manager.service.ManagerService;

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

@WebMvcTest(ManagerController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class ManagerControllerTest {

	private static final UUID MANAGER_ID = UUID.fromString("f0000000-0000-4000-8000-000000000001");
	private static final Instant CREATED_AT = Instant.parse("2026-01-01T00:00:00Z");
	private static final Instant UPDATED_AT = Instant.parse("2026-01-02T00:00:00Z");

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ManagerService managerService;

	@Test
	void list_returns200() throws Exception {
		when(managerService.getAll(null, null)).thenReturn(List.of(sampleResponse()));

		mockMvc.perform(get("/api/managers"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].code").value("ZA0001"));
	}

	@Test
	void getById_returns200() throws Exception {
		when(managerService.getById(MANAGER_ID)).thenReturn(sampleResponse());

		mockMvc.perform(get("/api/managers/{id}", MANAGER_ID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(MANAGER_ID.toString()))
				.andExpect(jsonPath("$.supervisorCount").value(2));
	}

	@Test
	void create_returns201() throws Exception {
		when(managerService.create(any())).thenReturn(sampleResponse());

		mockMvc.perform(post("/api/managers")
						.contentType(MediaType.APPLICATION_JSON)
						.content(validCreatePayload()))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.code").value("ZA0001"));
	}

	@Test
	void update_returns200() throws Exception {
		when(managerService.update(eq(MANAGER_ID), any())).thenReturn(sampleResponse());

		mockMvc.perform(put("/api/managers/{id}", MANAGER_ID)
						.contentType(MediaType.APPLICATION_JSON)
						.content(validUpdatePayload()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value("ZA0001"));
	}

	@Test
	void deactivate_returns204() throws Exception {
		mockMvc.perform(delete("/api/managers/{id}", MANAGER_ID))
				.andExpect(status().isNoContent());

		verify(managerService).deactivate(MANAGER_ID);
	}

	@Test
	void create_withInvalidRequest_returns400() throws Exception {
		mockMvc.perform(post("/api/managers")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "code": "",
								  "name": ""
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400));
	}

	@Test
	void create_withDuplicateName_returns409() throws Exception {
		when(managerService.create(any()))
				.thenThrow(new BusinessConflictException("Manager name already exists in organization"));

		mockMvc.perform(post("/api/managers")
						.contentType(MediaType.APPLICATION_JSON)
						.content(validCreatePayload()))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(409));
	}

	@Test
	void getById_whenNotFound_returns404() throws Exception {
		when(managerService.getById(MANAGER_ID))
				.thenThrow(new ResourceNotFoundException("Manager", MANAGER_ID));

		mockMvc.perform(get("/api/managers/{id}", MANAGER_ID))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404));
	}

	private ManagerResponse sampleResponse() {
		return new ManagerResponse(
				MANAGER_ID,
				"ZA0001",
				"Kozera Nieruchomości",
				"795702202",
				"kozera@example.com",
				"ul. Cupiała 7b",
				null,
				true,
				2,
				CREATED_AT,
				UPDATED_AT
		);
	}

	private String validCreatePayload() {
		return """
				{
				  "code": "ZA0001",
				  "name": "Kozera Nieruchomości",
				  "phone": "795702202",
				  "email": "kozera@example.com",
				  "address": "ul. Cupiała 7b"
				}
				""";
	}

	private String validUpdatePayload() {
		return """
				{
				  "code": "ZA0001",
				  "name": "Kozera Nieruchomości",
				  "phone": "795702202",
				  "email": "kozera@example.com",
				  "address": "ul. Cupiała 7b"
				}
				""";
	}
}
