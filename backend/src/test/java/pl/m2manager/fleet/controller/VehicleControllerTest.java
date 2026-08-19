package pl.m2manager.fleet.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pl.m2manager.common.exception.GlobalExceptionHandler;
import pl.m2manager.fleet.dto.response.VehicleResponse;
import pl.m2manager.fleet.entity.VehicleStatus;
import pl.m2manager.fleet.entity.VehicleType;
import pl.m2manager.fleet.service.VehicleService;

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

@WebMvcTest(VehicleController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class VehicleControllerTest {

	private static final UUID VEHICLE_ID = UUID.fromString("f4000000-0000-4000-8000-000000000001");
	private static final Instant CREATED_AT = Instant.parse("2026-01-01T00:00:00Z");
	private static final Instant UPDATED_AT = Instant.parse("2026-01-02T00:00:00Z");

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private VehicleService vehicleService;

	@Test
	void list_returns200() throws Exception {
		when(vehicleService.getAll(null, null, null, null)).thenReturn(List.of(sampleResponse()));

		mockMvc.perform(get("/api/fleet"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].code").value("DEMO-FL-001"));
	}

	@Test
	void getById_returns200() throws Exception {
		when(vehicleService.getById(VEHICLE_ID)).thenReturn(sampleResponse());

		mockMvc.perform(get("/api/fleet/{id}", VEHICLE_ID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(VEHICLE_ID.toString()));
	}

	@Test
	void create_returns201() throws Exception {
		when(vehicleService.create(any())).thenReturn(sampleResponse());

		mockMvc.perform(post("/api/fleet")
						.contentType(MediaType.APPLICATION_JSON)
						.content(validCreatePayload()))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.code").value("DEMO-FL-001"));
	}

	@Test
	void update_returns200() throws Exception {
		when(vehicleService.update(eq(VEHICLE_ID), any())).thenReturn(sampleResponse());

		mockMvc.perform(put("/api/fleet/{id}", VEHICLE_ID)
						.contentType(MediaType.APPLICATION_JSON)
						.content(validUpdatePayload()))
				.andExpect(status().isOk());
	}

	@Test
	void deactivate_returns204() throws Exception {
		mockMvc.perform(delete("/api/fleet/{id}", VEHICLE_ID))
				.andExpect(status().isNoContent());

		verify(vehicleService).deactivate(VEHICLE_ID);
	}

	@Test
	void create_withInvalidRequest_returns400() throws Exception {
		mockMvc.perform(post("/api/fleet")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "code": "",
								  "registrationNumber": "",
								  "make": "",
								  "model": ""
								}
								"""))
				.andExpect(status().isBadRequest());
	}

	private VehicleResponse sampleResponse() {
		return new VehicleResponse(
				VEHICLE_ID,
				"DEMO-FL-001",
				"SK DEMO01",
				"Demo",
				"Transit",
				2020,
				"DEMO0000000000001",
				VehicleType.VAN,
				null,
				null,
				null,
				VehicleStatus.ACTIVE,
				LocalDate.parse("2025-01-01"),
				LocalDate.parse("2027-03-15"),
				"Demo Insurer",
				"DEMO-POL-001",
				LocalDate.parse("2026-01-10"),
				LocalDate.parse("2027-02-10"),
				120000,
				LocalDate.parse("2020-06-01"),
				124500,
				null,
				CREATED_AT,
				UPDATED_AT
		);
	}

	private String validCreatePayload() {
		return """
				{
				  "code": "DEMO-FL-001",
				  "registrationNumber": "SK DEMO01",
				  "make": "Demo",
				  "model": "Transit",
				  "productionYear": 2020,
				  "vehicleType": "VAN",
				  "status": "ACTIVE"
				}
				""";
	}

	private String validUpdatePayload() {
		return validCreatePayload();
	}
}
