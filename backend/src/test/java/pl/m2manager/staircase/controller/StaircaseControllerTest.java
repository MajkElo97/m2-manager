package pl.m2manager.staircase.controller;

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
import pl.m2manager.staircase.dto.response.StaircaseResponse;
import pl.m2manager.staircase.service.StaircaseService;

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

@WebMvcTest(StaircaseController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class StaircaseControllerTest {

	private static final UUID BUILDING_ID = UUID.fromString("d0000000-0000-4000-8000-000000000001");
	private static final UUID STAIRCASE_ID = UUID.fromString("e0000000-0000-4000-8000-000000000001");
	private static final Instant CREATED_AT = Instant.parse("2026-01-01T00:00:00Z");
	private static final Instant UPDATED_AT = Instant.parse("2026-01-02T00:00:00Z");

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private StaircaseService staircaseService;

	@Test
	void list_returns200() throws Exception {
		when(staircaseService.getAll(BUILDING_ID)).thenReturn(List.of(sampleResponse()));

		mockMvc.perform(get("/api/staircases").param("buildingId", BUILDING_ID.toString()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].code").value("KL0001"));
	}

	@Test
	void listAllForOrganization_returns200() throws Exception {
		when(staircaseService.getAllForOrganization()).thenReturn(List.of(sampleResponse()));

		mockMvc.perform(get("/api/staircases"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].code").value("KL0001"));

		verify(staircaseService).getAllForOrganization();
	}

	@Test
	void getById_returns200() throws Exception {
		when(staircaseService.getById(STAIRCASE_ID)).thenReturn(sampleResponse());

		mockMvc.perform(get("/api/staircases/{id}", STAIRCASE_ID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(STAIRCASE_ID.toString()));
	}

	@Test
	void create_returns201() throws Exception {
		when(staircaseService.create(any())).thenReturn(sampleResponse());

		mockMvc.perform(post("/api/staircases")
						.contentType(MediaType.APPLICATION_JSON)
						.content(validCreatePayload()))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.code").value("KL0001"));
	}

	@Test
	void update_returns200() throws Exception {
		when(staircaseService.update(eq(STAIRCASE_ID), any())).thenReturn(sampleResponse());

		mockMvc.perform(put("/api/staircases/{id}", STAIRCASE_ID)
						.contentType(MediaType.APPLICATION_JSON)
						.content(validUpdatePayload()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value("KL0001"));
	}

	@Test
	void delete_returns204() throws Exception {
		mockMvc.perform(delete("/api/staircases/{id}", STAIRCASE_ID))
				.andExpect(status().isNoContent());

		verify(staircaseService).delete(STAIRCASE_ID);
	}

	@Test
	void create_withInvalidRequest_returns400() throws Exception {
		mockMvc.perform(post("/api/staircases")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "buildingId": "%s",
								  "code": "",
								  "designation": "",
								  "keyRequired": false,
								  "elevator": false,
								  "floors": -1
								}
								""".formatted(BUILDING_ID)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400));
	}

	@Test
	void create_withDuplicateCode_returns409() throws Exception {
		when(staircaseService.create(any()))
				.thenThrow(new BusinessConflictException("Staircase code already exists in organization"));

		mockMvc.perform(post("/api/staircases")
						.contentType(MediaType.APPLICATION_JSON)
						.content(validCreatePayload()))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(409));
	}

	@Test
	void getById_whenNotFound_returns404() throws Exception {
		when(staircaseService.getById(STAIRCASE_ID))
				.thenThrow(new ResourceNotFoundException("Staircase", STAIRCASE_ID));

		mockMvc.perform(get("/api/staircases/{id}", STAIRCASE_ID))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404));
	}

	private StaircaseResponse sampleResponse() {
		return new StaircaseResponse(
				STAIRCASE_ID,
				BUILDING_ID,
				"KL0001",
				"1",
				"#2258",
				false,
				false,
				4,
				null,
				CREATED_AT,
				UPDATED_AT
		);
	}

	private String validCreatePayload() {
		return """
				{
				  "buildingId": "%s",
				  "code": "KL0001",
				  "designation": "1",
				  "intercomCode": "#2258",
				  "keyRequired": false,
				  "elevator": false,
				  "floors": 4
				}
				""".formatted(BUILDING_ID);
	}

	private String validUpdatePayload() {
		return """
				{
				  "code": "KL0001",
				  "designation": "1",
				  "intercomCode": "#2258",
				  "keyRequired": false,
				  "elevator": false,
				  "floors": 4
				}
				""";
	}
}
