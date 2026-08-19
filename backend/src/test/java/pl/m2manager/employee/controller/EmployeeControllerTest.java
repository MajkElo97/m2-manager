package pl.m2manager.employee.controller;

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
import pl.m2manager.employee.dto.response.EmployeeResponse;
import pl.m2manager.employee.entity.EmployeeRole;
import pl.m2manager.employee.entity.EmploymentType;
import pl.m2manager.employee.entity.RemunerationUnit;
import pl.m2manager.employee.service.EmployeeService;

import java.math.BigDecimal;
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

@WebMvcTest(EmployeeController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class EmployeeControllerTest {

	private static final UUID EMPLOYEE_ID = UUID.fromString("f2000000-0000-4000-8000-000000000001");
	private static final Instant CREATED_AT = Instant.parse("2026-01-01T00:00:00Z");
	private static final Instant UPDATED_AT = Instant.parse("2026-01-02T00:00:00Z");

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private EmployeeService employeeService;

	@Test
	void list_returns200() throws Exception {
		when(employeeService.getAll(null, null, null, null, null)).thenReturn(List.of(sampleResponse()));

		mockMvc.perform(get("/api/employees"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].code").value("E0001"));
	}

	@Test
	void getById_returns200() throws Exception {
		when(employeeService.getById(EMPLOYEE_ID)).thenReturn(sampleResponse());

		mockMvc.perform(get("/api/employees/{id}", EMPLOYEE_ID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(EMPLOYEE_ID.toString()));
	}

	@Test
	void create_returns201() throws Exception {
		when(employeeService.create(any())).thenReturn(sampleResponse());

		mockMvc.perform(post("/api/employees")
						.contentType(MediaType.APPLICATION_JSON)
						.content(validCreatePayload()))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.code").value("E0001"));
	}

	@Test
	void update_returns200() throws Exception {
		when(employeeService.update(eq(EMPLOYEE_ID), any())).thenReturn(sampleResponse());

		mockMvc.perform(put("/api/employees/{id}", EMPLOYEE_ID)
						.contentType(MediaType.APPLICATION_JSON)
						.content(validUpdatePayload()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value("E0001"));
	}

	@Test
	void deactivate_returns204() throws Exception {
		mockMvc.perform(delete("/api/employees/{id}", EMPLOYEE_ID))
				.andExpect(status().isNoContent());

		verify(employeeService).deactivate(EMPLOYEE_ID);
	}

	@Test
	void create_withInvalidRequest_returns400() throws Exception {
		mockMvc.perform(post("/api/employees")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "code": "",
								  "firstName": "",
								  "role": null
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400));
	}

	@Test
	void create_withInvalidCalendarColor_returns400() throws Exception {
		mockMvc.perform(post("/api/employees")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "code": "E0001",
								  "firstName": "Jan",
								  "role": "PRACOWNIK",
								  "calendarColor": "F97316"
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400));
	}

	@Test
	void create_withDuplicateCode_returns409() throws Exception {
		when(employeeService.create(any()))
				.thenThrow(new BusinessConflictException("Employee code already exists in organization"));

		mockMvc.perform(post("/api/employees")
						.contentType(MediaType.APPLICATION_JSON)
						.content(validCreatePayload()))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(409));
	}

	@Test
	void getById_whenNotFound_returns404() throws Exception {
		when(employeeService.getById(EMPLOYEE_ID))
				.thenThrow(new ResourceNotFoundException("Employee", EMPLOYEE_ID));

		mockMvc.perform(get("/api/employees/{id}", EMPLOYEE_ID))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404));
	}

	private EmployeeResponse sampleResponse() {
		return new EmployeeResponse(
				EMPLOYEE_ID,
				"E0001",
				"Jadwiga",
				"Śliwa",
				"509481378",
				"jadwiga@gmail.com",
				"jadwiga@gmail.com",
				"Sprzątanie",
				EmployeeRole.PRACOWNIK,
				EmploymentType.ZLECENIE,
				LocalDate.of(2025, 5, 1),
				new BigDecimal("20.00"),
				RemunerationUnit.HOURLY,
				true,
				"#F97316",
				null,
				true,
				CREATED_AT,
				UPDATED_AT
		);
	}

	private String validCreatePayload() {
		return """
				{
				  "code": "E0001",
				  "firstName": "Jadwiga",
				  "lastName": "Śliwa",
				  "role": "PRACOWNIK",
				  "employmentType": "ZLECENIE",
				  "employmentStartDate": "2025-05-01",
				  "remunerationAmount": 20.00,
				  "remunerationUnit": "HOURLY",
				  "remunerationNet": true,
				  "calendarColor": "#F97316"
				}
				""";
	}

	private String validUpdatePayload() {
		return """
				{
				  "code": "E0001",
				  "firstName": "Jadwiga",
				  "lastName": "Śliwa",
				  "role": "PRACOWNIK",
				  "employmentType": "ZLECENIE",
				  "employmentStartDate": "2025-05-01",
				  "remunerationAmount": 20.00,
				  "remunerationUnit": "HOURLY",
				  "remunerationNet": true,
				  "calendarColor": "#F97316",
				  "active": true
				}
				""";
	}
}
