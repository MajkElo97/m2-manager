package pl.m2manager.role.controller;

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
import pl.m2manager.permission.dto.PermissionResponse;
import pl.m2manager.role.dto.RoleResponse;
import pl.m2manager.role.service.RolePermissionService;
import pl.m2manager.role.service.RoleService;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RoleController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class RoleControllerTest {

	private static final UUID ROLE_ID = UUID.fromString("b0000000-0000-4000-8000-000000000002");

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private RoleService roleService;

	@MockitoBean
	private RolePermissionService rolePermissionService;

	@Test
	void list_returns200() throws Exception {
		when(roleService.listRoles()).thenReturn(List.of(sampleResponse()));

		mockMvc.perform(get("/api/roles"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].name").value("ADMIN"));
	}

	@Test
	void getPermissions_returns200() throws Exception {
		when(rolePermissionService.listPermissions(ROLE_ID)).thenReturn(List.of(
				new PermissionResponse("BUILDINGS_VIEW", "BUILDINGS", "VIEW", "VIEW BUILDINGS module")
		));

		mockMvc.perform(get("/api/roles/{id}/permissions", ROLE_ID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].code").value("BUILDINGS_VIEW"));
	}

	@Test
	void replacePermissions_returns204() throws Exception {
		mockMvc.perform(put("/api/roles/{id}/permissions", ROLE_ID)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "permissionCodes": ["BUILDINGS_VIEW", "BUILDINGS_CREATE"]
								}
								"""))
				.andExpect(status().isNoContent());

		verify(rolePermissionService).replacePermissions(eq(ROLE_ID), any());
	}

	@Test
	void deactivate_systemRole_returns409() throws Exception {
		doThrow(new BusinessConflictException("Cannot deactivate a system role"))
				.when(roleService).deactivateRole(ROLE_ID);

		mockMvc.perform(delete("/api/roles/{id}", ROLE_ID))
				.andExpect(status().isConflict());
	}

	@Test
	void create_returns201() throws Exception {
		when(roleService.createRole(any())).thenReturn(sampleResponse());

		mockMvc.perform(post("/api/roles")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "Custom",
								  "description": "Test role"
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.name").value("ADMIN"));
	}

	private RoleResponse sampleResponse() {
		return new RoleResponse(ROLE_ID, "ADMIN", "Admin role", false, true, 1, 4);
	}
}
