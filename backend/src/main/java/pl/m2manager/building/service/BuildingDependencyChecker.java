package pl.m2manager.building.service;

import org.springframework.stereotype.Component;
import pl.m2manager.contact.repository.ContactRepository;
import pl.m2manager.finance.repository.FinancialTransactionRepository;
import pl.m2manager.scope.repository.ActivityScopeRepository;
import pl.m2manager.staircase.repository.StaircaseRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class BuildingDependencyChecker {

	private final StaircaseRepository staircaseRepository;
	private final ActivityScopeRepository activityScopeRepository;
	private final ContactRepository contactRepository;
	private final FinancialTransactionRepository financialTransactionRepository;

	public BuildingDependencyChecker(
			StaircaseRepository staircaseRepository,
			ActivityScopeRepository activityScopeRepository,
			ContactRepository contactRepository,
			FinancialTransactionRepository financialTransactionRepository
	) {
		this.staircaseRepository = staircaseRepository;
		this.activityScopeRepository = activityScopeRepository;
		this.contactRepository = contactRepository;
		this.financialTransactionRepository = financialTransactionRepository;
	}

	public BuildingDependencyCounts countDependencies(UUID organizationId, UUID buildingId) {
		long staircases = staircaseRepository.countByOrganizationIdAndBuildingId(organizationId, buildingId);
		long scopes = activityScopeRepository.countByOrganizationIdAndBuildingId(organizationId, buildingId);
		long contacts = contactRepository.countByOrganizationIdAndBuildingId(organizationId, buildingId);
		long financialTransactions = financialTransactionRepository.countByOrganizationIdAndBuildingId(
				organizationId,
				buildingId
		);
		return new BuildingDependencyCounts(staircases, scopes, contacts, financialTransactions);
	}

	public record BuildingDependencyCounts(
			long staircases,
			long scopes,
			long contacts,
			long financialTransactions
	) {
		public boolean hasBlockingDependencies() {
			return staircases > 0 || scopes > 0 || contacts > 0 || financialTransactions > 0;
		}

		public String formatConflictMessage() {
			List<String> parts = new ArrayList<>();
			if (staircases > 0) {
				parts.add(formatCount(staircases, "klatka", "klatki", "klatk"));
			}
			if (scopes > 0) {
				parts.add(formatCount(scopes, "zakres", "zakresy", "zakres"));
			}
			if (contacts > 0) {
				parts.add(formatCount(contacts, "kontakt", "kontakty", "kontakt"));
			}
			if (financialTransactions > 0) {
				parts.add(formatCount(financialTransactions, "transakcja finansowa", "transakcje finansowe", "transakcji finansowych"));
			}
			return "Nie można usunąć budynku, ponieważ posiada: " + String.join(", ", parts);
		}

		private static String formatCount(long count, String singular, String few, String many) {
			if (count == 1) {
				return "1 " + singular;
			}
			if (count >= 2 && count <= 4) {
				return count + " " + few;
			}
			return count + " " + many;
		}
	}
}
