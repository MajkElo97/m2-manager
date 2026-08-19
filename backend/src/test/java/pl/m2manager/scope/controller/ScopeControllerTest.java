package pl.m2manager.scope.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pl.m2manager.common.exception.GlobalExceptionHandler;
import pl.m2manager.scope.dto.response.ScopeResponse;
import pl.m2manager.scope.entity.ScopePlanningType;
import pl.m2manager.scope.entity.ScopeStatus;
import pl.m2manager.scope.service.ScopeService;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ScopeController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class ScopeControllerTest {

	private static final UUID SCOPE_ID = UUID.fromString("01000000-0000-4000-8000-000000000001");
	private static final UUID BUILDING_ID = UUID.fromString("d0000000-0000-4000-8000-000000000001");
	private static final UUID ACTIVITY_ID = UUID.fromString("f0000000-0000-4000-8000-000000000001");
	private static final Instant CREATED_AT = Instant.parse("2026-01-01T00:00:00Z");
	private static final Instant UPDATED_AT = Instant.parse("2026-01-02T00:00:00Z");

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ScopeService scopeService;

	@Test
	void list_returns200() throws Exception {
		when(scopeService.getAll(null, null, null, null, null)).thenReturn(List.of(sampleResponse()));

		mockMvc.perform(get("/api/scopes"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].code").value("ZP0001"));
	}

	@Test
	void list_withBuildingId_returns200() throws Exception {
		when(scopeService.getAll(BUILDING_ID, null, null, null, null)).thenReturn(List.of(sampleResponse()));

		mockMvc.perform(get("/api/scopes").param("buildingId", BUILDING_ID.toString()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].code").value("ZP0001"));
	}

	@Test
	void create_returns201() throws Exception {
		when(scopeService.create(any())).thenReturn(sampleResponse());

		mockMvc.perform(post("/api/scopes")
						.contentType(MediaType.APPLICATION_JSON)
						.content(validCreatePayload()))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.code").value("ZP0001"));
	}

	@Test
	void deactivate_returns204() throws Exception {
		mockMvc.perform(delete("/api/scopes/{id}", SCOPE_ID))
				.andExpect(status().isNoContent());

		verify(scopeService).deactivate(SCOPE_ID);
	}

	private ScopeResponse sampleResponse() {
		return new ScopeResponse(
				SCOPE_ID,
				"ZP0001",
				BUILDING_ID,
				ACTIVITY_ID,
				ScopePlanningType.WEEKLY,
				1,
				"Wtorek",
				null,
				ScopeStatus.ACTIVE,
				CREATED_AT,
				UPDATED_AT
		);
	}

	private String validCreatePayload() {
		return """
				{
				  "code": "ZP0001",
				  "buildingId": "%s",
				  "activityId": "%s",
				  "planningType": "WEEKLY",
				  "frequency": 1,
				  "weekdays": "Wtorek"
				}
				""".formatted(BUILDING_ID, ACTIVITY_ID);
	}
}
