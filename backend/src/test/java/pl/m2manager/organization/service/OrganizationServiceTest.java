package pl.m2manager.organization.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import pl.m2manager.common.exception.ResourceNotFoundException;
import pl.m2manager.organization.dto.request.UpdateOrganizationRequest;
import pl.m2manager.organization.dto.response.OrganizationResponse;
import pl.m2manager.organization.entity.Organization;
import pl.m2manager.organization.mapper.OrganizationMapper;
import pl.m2manager.organization.repository.OrganizationRepository;
import pl.m2manager.tenant.TenantContext;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {

	private static final UUID ORGANIZATION_ID = UUID.fromString("a0000000-0000-4000-8000-000000000001");

	@Mock
	private OrganizationRepository organizationRepository;

	@Mock
	private TenantContext tenantContext;

	@Mock
	private OrganizationMapper organizationMapper;

	@InjectMocks
	private OrganizationService organizationService;

	private Organization organization;
	private OrganizationResponse organizationResponse;

	@BeforeEach
	void setUp() {
		organization = new Organization();
		ReflectionTestUtils.setField(organization, "id", ORGANIZATION_ID);
		organization.setName("M2 Manager Dev");
		organization.setNip("1234567890");
		organization.setEmail("dev@m2manager.local");
		organization.setPhone("+48123456789");
		organization.setTimezone("Europe/Warsaw");

		organizationResponse = new OrganizationResponse(
				ORGANIZATION_ID,
				"M2 Manager Dev",
				"1234567890",
				"dev@m2manager.local",
				"+48123456789",
				true,
				"Europe/Warsaw",
				Instant.parse("2026-01-01T00:00:00Z"),
				Instant.parse("2026-01-01T00:00:00Z")
		);
	}

	@Test
	void getCurrentOrganization_returnsCurrentOrganization() {
		when(tenantContext.getCurrentOrganizationId()).thenReturn(ORGANIZATION_ID);
		when(organizationRepository.findById(ORGANIZATION_ID)).thenReturn(Optional.of(organization));
		when(organizationMapper.toResponse(organization)).thenReturn(organizationResponse);

		OrganizationResponse result = organizationService.getCurrentOrganization();

		assertThat(result).isEqualTo(organizationResponse);
		verify(tenantContext).getCurrentOrganizationId();
		verify(organizationRepository).findById(ORGANIZATION_ID);
	}

	@Test
	void getCurrentOrganization_throwsWhenOrganizationDoesNotExist() {
		when(tenantContext.getCurrentOrganizationId()).thenReturn(ORGANIZATION_ID);
		when(organizationRepository.findById(ORGANIZATION_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> organizationService.getCurrentOrganization())
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessageContaining("Organization not found");
	}

	@Test
	void updateCurrentOrganization_updatesEditableFields() {
		UpdateOrganizationRequest request = new UpdateOrganizationRequest(
				"Updated Name",
				"9988776655",
				"updated@example.com",
				"+48987654321",
				"Europe/Berlin"
		);

		when(tenantContext.getCurrentOrganizationId()).thenReturn(ORGANIZATION_ID);
		when(organizationRepository.findById(ORGANIZATION_ID)).thenReturn(Optional.of(organization));
		when(organizationRepository.save(organization)).thenAnswer(invocation -> invocation.getArgument(0));
		when(organizationMapper.toResponse(organization)).thenReturn(
				new OrganizationResponse(
						ORGANIZATION_ID,
						"Updated Name",
						"9988776655",
						"updated@example.com",
						"+48987654321",
						true,
						"Europe/Berlin",
						Instant.parse("2026-01-01T00:00:00Z"),
						Instant.parse("2026-01-02T00:00:00Z")
				)
		);

		OrganizationResponse result = organizationService.updateCurrentOrganization(request);

		assertThat(result.name()).isEqualTo("Updated Name");
		assertThat(result.nip()).isEqualTo("9988776655");
		assertThat(result.email()).isEqualTo("updated@example.com");
		assertThat(result.phone()).isEqualTo("+48987654321");
		assertThat(result.timezone()).isEqualTo("Europe/Berlin");
		assertThat(organization.getName()).isEqualTo("Updated Name");
		assertThat(organization.getTimezone()).isEqualTo("Europe/Berlin");
	}

	@Test
	void updateCurrentOrganization_doesNotChangeId() {
		UUID originalId = ORGANIZATION_ID;
		organizationResponse = new OrganizationResponse(
				originalId,
				organization.getName(),
				organization.getNip(),
				organization.getEmail(),
				organization.getPhone(),
				true,
				organization.getTimezone(),
				Instant.parse("2026-01-01T00:00:00Z"),
				Instant.parse("2026-01-01T00:00:00Z")
		);

		UpdateOrganizationRequest request = new UpdateOrganizationRequest(
				"Updated Name",
				organization.getNip(),
				organization.getEmail(),
				organization.getPhone(),
				organization.getTimezone()
		);

		when(tenantContext.getCurrentOrganizationId()).thenReturn(ORGANIZATION_ID);
		when(organizationRepository.findById(ORGANIZATION_ID)).thenReturn(Optional.of(organization));
		when(organizationRepository.save(any(Organization.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(organizationMapper.toResponse(organization)).thenReturn(organizationResponse);

		organizationService.updateCurrentOrganization(request);

		ArgumentCaptor<Organization> captor = ArgumentCaptor.forClass(Organization.class);
		verify(organizationRepository).save(captor.capture());
		assertThat(captor.getValue().getId()).isEqualTo(originalId);
	}

	@Test
	void updateCurrentOrganization_doesNotChangeActive() {
		UpdateOrganizationRequest request = new UpdateOrganizationRequest(
				"Updated Name",
				null,
				null,
				null,
				"Europe/Warsaw"
		);

		when(tenantContext.getCurrentOrganizationId()).thenReturn(ORGANIZATION_ID);
		when(organizationRepository.findById(ORGANIZATION_ID)).thenReturn(Optional.of(organization));
		when(organizationRepository.save(any(Organization.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(organizationMapper.toResponse(organization)).thenReturn(organizationResponse);

		organizationService.updateCurrentOrganization(request);

		ArgumentCaptor<Organization> captor = ArgumentCaptor.forClass(Organization.class);
		verify(organizationRepository).save(captor.capture());
		assertThat(captor.getValue().isActive()).isTrue();
	}

	@Test
	void updateCurrentOrganization_usesTenantContextForOrganizationId() {
		UpdateOrganizationRequest request = new UpdateOrganizationRequest(
				"Updated Name",
				null,
				null,
				null,
				"Europe/Warsaw"
		);

		when(tenantContext.getCurrentOrganizationId()).thenReturn(ORGANIZATION_ID);
		when(organizationRepository.findById(ORGANIZATION_ID)).thenReturn(Optional.of(organization));
		when(organizationRepository.save(organization)).thenReturn(organization);
		when(organizationMapper.toResponse(organization)).thenReturn(organizationResponse);

		organizationService.updateCurrentOrganization(request);

		verify(tenantContext).getCurrentOrganizationId();
		verify(organizationRepository).findById(ORGANIZATION_ID);
	}
}
