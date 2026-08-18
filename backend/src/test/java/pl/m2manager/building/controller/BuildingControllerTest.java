package pl.m2manager.building.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pl.m2manager.building.dto.response.BuildingResponse;
import pl.m2manager.building.entity.BuildingStatus;
import pl.m2manager.building.service.BuildingService;
import pl.m2manager.common.exception.BusinessConflictException;
import pl.m2manager.common.exception.GlobalExceptionHandler;
import pl.m2manager.common.exception.ResourceNotFoundException;

import java.time.Instant;
import java.time.LocalDate;
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

@WebMvcTest(BuildingController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class BuildingControllerTest {

	private static final UUID BUILDING_ID = UUID.fromString("d0000000-0000-4000-8000-000000000001");
	private static final Instant CREATED_AT = Instant.parse("2026-01-01T00:00:00Z");
	private static final Instant UPDATED_AT = Instant.parse("2026-01-02T00:00:00Z");

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private BuildingService buildingService;

	@Test
	void list_returns200() throws Exception {
		when(buildingService.getAll(null, null)).thenReturn(List.of(sampleResponse()));

		mockMvc.perform(get("/api/buildings"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].code").value("PUSTA64"));
	}

	@Test
	void getById_returns200() throws Exception {
		when(buildingService.getById(BUILDING_ID)).thenReturn(sampleResponse());

		mockMvc.perform(get("/api/buildings/{id}", BUILDING_ID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(BUILDING_ID.toString()));
	}

	@Test
	void create_returns201() throws Exception {
		when(buildingService.create(any())).thenReturn(sampleResponse());

		mockMvc.perform(post("/api/buildings")
						.contentType(MediaType.APPLICATION_JSON)
						.content(validCreatePayload()))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.code").value("PUSTA64"));
	}

	@Test
	void update_returns200() throws Exception {
		when(buildingService.update(eq(BUILDING_ID), any())).thenReturn(sampleResponse());

		mockMvc.perform(put("/api/buildings/{id}", BUILDING_ID)
						.contentType(MediaType.APPLICATION_JSON)
						.content(validUpdatePayload()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value("PUSTA64"));
	}

	@Test
	void deactivate_returns204() throws Exception {
		mockMvc.perform(delete("/api/buildings/{id}", BUILDING_ID))
				.andExpect(status().isNoContent());

		verify(buildingService).deactivate(BUILDING_ID);
	}

	@Test
	void create_withInvalidRequest_returns400() throws Exception {
		mockMvc.perform(post("/api/buildings")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "code": "",
								  "name": "",
								  "address": "",
								  "city": "",
								  "noticePeriodMonths": -1
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400));
	}

	@Test
	void create_withDuplicateCode_returns409() throws Exception {
		when(buildingService.create(any()))
				.thenThrow(new BusinessConflictException("Building code already exists in organization"));

		mockMvc.perform(post("/api/buildings")
						.contentType(MediaType.APPLICATION_JSON)
						.content(validCreatePayload()))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(409));
	}

	@Test
	void getById_whenNotFound_returns404() throws Exception {
		when(buildingService.getById(BUILDING_ID))
				.thenThrow(new ResourceNotFoundException("Building", BUILDING_ID));

		mockMvc.perform(get("/api/buildings/{id}", BUILDING_ID))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404));
	}

	private BuildingResponse sampleResponse() {
		return new BuildingResponse(
				BUILDING_ID,
				"PUSTA64",
				"Pusta 64",
				"ul. Pusta 64",
				"Sosnowiec",
				"6443561947",
				null,
				null,
				"ZA0001",
				"OP0001",
				"E0001",
				null,
				LocalDate.of(2025, 1, 5),
				3,
				BuildingStatus.ACTIVE,
				null,
				CREATED_AT,
				UPDATED_AT
		);
	}

	private String validCreatePayload() {
		return """
				{
				  "code": "PUSTA64",
				  "name": "Pusta 64",
				  "address": "ul. Pusta 64",
				  "city": "Sosnowiec",
				  "noticePeriodMonths": 3,
				  "serviceStartDate": "2025-01-05"
				}
				""";
	}

	private String validUpdatePayload() {
		return """
				{
				  "code": "PUSTA64",
				  "name": "Pusta 64",
				  "address": "ul. Pusta 64",
				  "city": "Sosnowiec",
				  "noticePeriodMonths": 3,
				  "status": "ACTIVE",
				  "serviceStartDate": "2025-01-05"
				}
				""";
	}
}
