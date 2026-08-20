package pl.m2manager.finance.mapper;

import org.springframework.stereotype.Component;
import pl.m2manager.finance.dto.request.CreateFinancialCategoryRequest;
import pl.m2manager.finance.dto.request.UpdateFinancialCategoryRequest;
import pl.m2manager.finance.dto.response.FinancialCategoryResponse;
import pl.m2manager.finance.entity.FinancialCategory;

@Component
public class FinancialCategoryMapper {

	public FinancialCategoryResponse toResponse(FinancialCategory category) {
		return new FinancialCategoryResponse(
				category.getId(),
				category.getCode(),
				category.getName(),
				category.getType(),
				category.isActive(),
				category.getCreatedAt(),
				category.getUpdatedAt()
		);
	}

	public void applyCreate(FinancialCategory category, CreateFinancialCategoryRequest request) {
		category.setCode(request.code());
		category.setName(request.name());
		category.setType(request.type());
		category.setActive(request.active());
	}

	public void applyUpdate(FinancialCategory category, UpdateFinancialCategoryRequest request) {
		category.setCode(request.code());
		category.setName(request.name());
		category.setType(request.type());
		category.setActive(request.active());
	}
}
