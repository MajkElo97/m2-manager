package pl.m2manager.permission.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.m2manager.common.exception.ResourceNotFoundException;
import pl.m2manager.permission.dto.PermissionResponse;
import pl.m2manager.permission.repository.PermissionRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class PermissionServiceTest {

	@Container
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine")
			.withDatabaseName("m2manager_test")
			.withUsername("m2manager")
			.withPassword("m2manager_test");

	@DynamicPropertySource
	static void configureDataSource(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);
	}

	@Autowired
	private PermissionService permissionService;

	@Autowired
	private PermissionRepository permissionRepository;

	@Test
	void listAll_returnsExactly85Permissions() {
		List<PermissionResponse> permissions = permissionService.listAll();

		assertThat(permissions).hasSize(85);
		assertThat(permissionRepository.count()).isEqualTo(85);
	}

	@Test
	void findByCode_returnsPermission() {
		PermissionResponse permission = permissionService.findByCode("BUILDINGS_VIEW");

		assertThat(permission.code()).isEqualTo("BUILDINGS_VIEW");
		assertThat(permission.module()).isEqualTo("BUILDINGS");
		assertThat(permission.action()).isEqualTo("VIEW");
	}

	@Test
	void findByModule_returnsModulePermissions() {
		List<PermissionResponse> buildingsPermissions = permissionService.findByModule("BUILDINGS");

		assertThat(buildingsPermissions).hasSize(5);
		assertThat(buildingsPermissions).extracting(PermissionResponse::code)
				.containsExactlyInAnyOrder(
						"BUILDINGS_VIEW",
						"BUILDINGS_CREATE",
						"BUILDINGS_EDIT",
						"BUILDINGS_DELETE",
						"BUILDINGS_ADMIN"
				);
	}

	@Test
	void findByCodes_returnsMatchingPermissions() {
		List<PermissionResponse> permissions = permissionService.findByCodes(List.of("BUILDINGS_VIEW", "SCHEDULE_EDIT"));

		assertThat(permissions).hasSize(2);
		assertThat(permissions).extracting(PermissionResponse::code)
				.containsExactlyInAnyOrder("BUILDINGS_VIEW", "SCHEDULE_EDIT");
	}

	@Test
	void findByCode_unknownPermission_throwsNotFound() {
		assertThatThrownBy(() -> permissionService.findByCode("UNKNOWN_PERMISSION"))
				.isInstanceOf(ResourceNotFoundException.class);
	}
}
