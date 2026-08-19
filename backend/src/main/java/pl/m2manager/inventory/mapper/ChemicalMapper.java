package pl.m2manager.inventory.mapper;

import org.springframework.stereotype.Component;
import pl.m2manager.inventory.dto.request.CreateChemicalRequest;
import pl.m2manager.inventory.dto.request.UpdateChemicalRequest;
import pl.m2manager.inventory.dto.response.ChemicalResponse;
import pl.m2manager.inventory.entity.Chemical;

@Component
public class ChemicalMapper {

	public ChemicalResponse toResponse(Chemical chemical) {
		return new ChemicalResponse(
				chemical.getId(),
				chemical.getCode(),
				chemical.getName(),
				chemical.getCategory(),
				chemical.getQuantity(),
				chemical.getUnit(),
				chemical.getMinimumStock(),
				isLowStock(chemical),
				chemical.getLocation(),
				chemical.isActive(),
				chemical.getNotes(),
				chemical.getCreatedAt(),
				chemical.getUpdatedAt()
		);
	}

	public void applyCreate(Chemical chemical, CreateChemicalRequest request) {
		chemical.setCode(request.code());
		chemical.setName(request.name());
		chemical.setCategory(request.category());
		chemical.setQuantity(request.quantity());
		chemical.setUnit(request.unit());
		chemical.setMinimumStock(request.minimumStock());
		chemical.setLocation(request.location());
		chemical.setActive(request.active());
		chemical.setNotes(request.notes());
	}

	public void applyUpdate(Chemical chemical, UpdateChemicalRequest request) {
		chemical.setCode(request.code());
		chemical.setName(request.name());
		chemical.setCategory(request.category());
		chemical.setQuantity(request.quantity());
		chemical.setUnit(request.unit());
		chemical.setMinimumStock(request.minimumStock());
		chemical.setLocation(request.location());
		chemical.setActive(request.active());
		chemical.setNotes(request.notes());
	}

	private boolean isLowStock(Chemical chemical) {
		if (chemical.getMinimumStock() == null) {
			return false;
		}
		return chemical.getQuantity().compareTo(chemical.getMinimumStock()) < 0;
	}
}
