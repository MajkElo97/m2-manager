package pl.m2manager.activity.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pl.m2manager.activity.dto.response.ActivityResponse;
import pl.m2manager.activity.entity.ActivityPlanningType;
import pl.m2manager.activity.entity.ActivityPriority;
import pl.m2manager.activity.service.ActivityService;
import pl.m2manager.common.exception.GlobalExceptionHandler;

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

@WebMvcTest(ActivityController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class ActivityControllerTest {

	private static final UUID ACTIVITY_ID = UUID.fromString("f0000000-0000-4000-8000-000000000001");
	private static final Instant CREATED_AT = Instant.parse("2026-01-01T00:00:00Z");
	private static final Instant UPDATED_AT = Instant.parse("2026-01-02T00:00:00Z");

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ActivityService activityService;

	@Test
	void list_returns200() throws Exception {
		when(activityService.getAll(null, null, null, null)).thenReturn(List.of(sampleResponse()));

		mockMvc.perform(get("/api/activities"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].code").value("CZ0001"));
	}

	@Test
	void create_returns201() throws Exception {
		when(activityService.create(any())).thenReturn(sampleResponse());

		mockMvc.perform(post("/api/activities")
						.contentType(MediaType.APPLICATION_JSON)
						.content(validCreatePayload()))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.code").value("CZ0001"));
	}

	@Test
	void deactivate_returns204() throws Exception {
		mockMvc.perform(delete("/api/activities/{id}", ACTIVITY_ID))
				.andExpect(status().isNoContent());

		verify(activityService).deactivate(ACTIVITY_ID);
	}

	@Test
	void update_returns200() throws Exception {
		when(activityService.update(eq(ACTIVITY_ID), any())).thenReturn(sampleResponse());

		mockMvc.perform(put("/api/activities/{id}", ACTIVITY_ID)
						.contentType(MediaType.APPLICATION_JSON)
						.content(validUpdatePayload()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value("CZ0001"));
	}

	private ActivityResponse sampleResponse() {
		return new ActivityResponse(
				ACTIVITY_ID,
				"CZ0001",
				"Tereny zewnętrzne",
				"Sprzątanie",
				ActivityPlanningType.CYCLIC,
				null,
				30,
				ActivityPriority.NORMAL,
				true,
				CREATED_AT,
				UPDATED_AT
		);
	}

	private String validCreatePayload() {
		return """
				{
				  "code": "CZ0001",
				  "name": "Tereny zewnętrzne",
				  "category": "Sprzątanie",
				  "planningType": "CYCLIC",
				  "durationMinutes": 30,
				  "priority": "NORMAL"
				}
				""";
	}

	private String validUpdatePayload() {
		return """
				{
				  "code": "CZ0001",
				  "name": "Tereny zewnętrzne",
				  "category": "Sprzątanie",
				  "planningType": "CYCLIC",
				  "durationMinutes": 30,
				  "priority": "NORMAL",
				  "active": true
				}
				""";
	}
}
