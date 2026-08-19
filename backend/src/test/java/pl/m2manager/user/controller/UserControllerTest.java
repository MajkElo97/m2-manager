package pl.m2manager.user.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pl.m2manager.common.exception.GlobalExceptionHandler;
import pl.m2manager.user.dto.UserResponse;
import pl.m2manager.user.dto.UserRoleSummary;
import pl.m2manager.user.service.UserService;

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

@WebMvcTest(UserController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

	private static final UUID USER_ID = UUID.fromString("b0000000-0000-4000-8000-000000000001");
	private static final UUID ROLE_ID = UUID.fromString("b0000000-0000-4000-8000-000000000002");
	private static final Instant CREATED_AT = Instant.parse("2026-01-01T00:00:00Z");
	private static final Instant UPDATED_AT = Instant.parse("2026-01-02T00:00:00Z");

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UserService userService;

	@Test
	void list_returns200() throws Exception {
		when(userService.getAll(null, null, null)).thenReturn(List.of(sampleResponse()));

		mockMvc.perform(get("/api/users"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].email").value("jan@example.com"));
	}

	@Test
	void getById_returns200() throws Exception {
		when(userService.getById(USER_ID)).thenReturn(sampleResponse());

		mockMvc.perform(get("/api/users/{id}", USER_ID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(USER_ID.toString()));
	}

	@Test
	void create_returns201() throws Exception {
		when(userService.create(any())).thenReturn(sampleResponse());

		mockMvc.perform(post("/api/users")
						.contentType(MediaType.APPLICATION_JSON)
						.content(validCreatePayload()))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.firstName").value("Jan"));
	}

	@Test
	void update_returns200() throws Exception {
		when(userService.update(eq(USER_ID), any())).thenReturn(sampleResponse());

		mockMvc.perform(put("/api/users/{id}", USER_ID)
						.contentType(MediaType.APPLICATION_JSON)
						.content(validUpdatePayload()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value("jan@example.com"));
	}

	@Test
	void deactivate_returns204() throws Exception {
		mockMvc.perform(delete("/api/users/{id}", USER_ID))
				.andExpect(status().isNoContent());

		verify(userService).deactivate(USER_ID);
	}

	@Test
	void create_withInvalidRequest_returns400() throws Exception {
		mockMvc.perform(post("/api/users")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "firstName": "",
								  "lastName": "Kowalski",
								  "email": "invalid",
								  "password": "short",
								  "roleIds": []
								}
								"""))
				.andExpect(status().isBadRequest());
	}

	private UserResponse sampleResponse() {
		return new UserResponse(
				USER_ID,
				"Jan",
				"Kowalski",
				"jan@example.com",
				true,
				List.of(new UserRoleSummary(ROLE_ID, "ADMIN", false)),
				null,
				null,
				null,
				CREATED_AT,
				UPDATED_AT
		);
	}

	private String validCreatePayload() {
		return """
				{
				  "firstName": "Jan",
				  "lastName": "Kowalski",
				  "email": "jan@example.com",
				  "password": "password123",
				  "roleIds": ["%s"]
				}
				""".formatted(ROLE_ID);
	}

	private String validUpdatePayload() {
		return """
				{
				  "firstName": "Jan",
				  "lastName": "Kowalski",
				  "email": "jan@example.com",
				  "roleIds": ["%s"],
				  "active": true
				}
				""".formatted(ROLE_ID);
	}
}
