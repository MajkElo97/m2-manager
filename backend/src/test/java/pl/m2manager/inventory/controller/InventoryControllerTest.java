package pl.m2manager.inventory.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pl.m2manager.common.exception.GlobalExceptionHandler;
import pl.m2manager.inventory.dto.response.ChemicalResponse;
import pl.m2manager.inventory.dto.response.EquipmentResponse;
import pl.m2manager.inventory.entity.ChemicalUnit;
import pl.m2manager.inventory.entity.EquipmentCondition;
import pl.m2manager.inventory.service.ChemicalService;
import pl.m2manager.inventory.service.EquipmentService;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
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

@WebMvcTest({ EquipmentController.class, ChemicalController.class })
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class InventoryControllerTest {

	private static final UUID EQUIPMENT_ID = UUID.fromString("f5000000-0000-4000-8000-000000000001");
	private static final UUID CHEMICAL_ID = UUID.fromString("f6000000-0000-4000-8000-000000000001");
	private static final Instant CREATED_AT = Instant.parse("2026-01-01T00:00:00Z");
	private static final Instant UPDATED_AT = Instant.parse("2026-01-02T00:00:00Z");

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private EquipmentService equipmentService;

	@MockitoBean
	private ChemicalService chemicalService;

	@Test
	void equipmentList_returns200() throws Exception {
		when(equipmentService.getAll(null, null, null, null, null)).thenReturn(List.of(sampleEquipmentResponse()));

		mockMvc.perform(get("/api/inventory/equipment"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].code").value("DEMO-EQ-001"));
	}

	@Test
	void equipmentCreate_returns201() throws Exception {
		when(equipmentService.create(any())).thenReturn(sampleEquipmentResponse());

		mockMvc.perform(post("/api/inventory/equipment")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "code": "DEMO-EQ-001",
								  "name": "Demo",
								  "category": "Cleaning",
								  "quantity": 1,
								  "conditionStatus": "GOOD",
								  "active": true
								}
								"""))
				.andExpect(status().isCreated());
	}

	@Test
	void equipmentDeactivate_returns204() throws Exception {
		mockMvc.perform(delete("/api/inventory/equipment/{id}", EQUIPMENT_ID))
				.andExpect(status().isNoContent());

		verify(equipmentService).deactivate(EQUIPMENT_ID);
	}

	@Test
	void chemicalsList_returns200() throws Exception {
		when(chemicalService.getAll(null, null, null, null)).thenReturn(List.of(sampleChemicalResponse()));

		mockMvc.perform(get("/api/inventory/chemicals"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].code").value("DEMO-CH-001"));
	}

	@Test
	void chemicalsCreate_returns201() throws Exception {
		when(chemicalService.create(any())).thenReturn(sampleChemicalResponse());

		mockMvc.perform(post("/api/inventory/chemicals")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "code": "DEMO-CH-001",
								  "name": "Demo",
								  "category": "Windows",
								  "quantity": 12,
								  "unit": "LITER",
								  "active": true
								}
								"""))
				.andExpect(status().isCreated());
	}

	@Test
	void chemicalsDeactivate_returns204() throws Exception {
		mockMvc.perform(delete("/api/inventory/chemicals/{id}", CHEMICAL_ID))
				.andExpect(status().isNoContent());

		verify(chemicalService).deactivate(CHEMICAL_ID);
	}

	private EquipmentResponse sampleEquipmentResponse() {
		return new EquipmentResponse(
				EQUIPMENT_ID,
				"DEMO-EQ-001",
				"Demo",
				"Cleaning",
				null,
				null,
				null,
				2,
				EquipmentCondition.GOOD,
				"Magazyn",
				null,
				null,
				null,
				LocalDate.parse("2024-03-01"),
				new BigDecimal("1500.00"),
				true,
				null,
				CREATED_AT,
				UPDATED_AT
		);
	}

	private ChemicalResponse sampleChemicalResponse() {
		return new ChemicalResponse(
				CHEMICAL_ID,
				"DEMO-CH-001",
				"Demo",
				"Windows",
				new BigDecimal("12.000"),
				ChemicalUnit.LITER,
				new BigDecimal("5.000"),
				false,
				"Magazyn",
				true,
				null,
				CREATED_AT,
				UPDATED_AT
		);
	}
}
