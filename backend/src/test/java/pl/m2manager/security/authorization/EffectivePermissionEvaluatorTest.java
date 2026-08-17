package pl.m2manager.security.authorization;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class EffectivePermissionEvaluatorTest {

	@Test
	void directPermission_isAuthorized() {
		Set<String> codes = Set.of("SCHEDULE_VIEW", "SCHEDULE_EDIT");

		assertThat(EffectivePermissionEvaluator.hasPermission(codes, "SCHEDULE_VIEW")).isTrue();
		assertThat(EffectivePermissionEvaluator.hasPermission(codes, "SCHEDULE_EDIT")).isTrue();
		assertThat(EffectivePermissionEvaluator.hasPermission(codes, "SCHEDULE_CREATE")).isFalse();
		assertThat(EffectivePermissionEvaluator.hasPermission(codes, "SCHEDULE_DELETE")).isFalse();
		assertThat(EffectivePermissionEvaluator.hasPermission(codes, "SCHEDULE_ADMIN")).isFalse();
	}

	@Test
	void moduleAdmin_authorizesAllModuleActions() {
		Set<String> codes = Set.of("BUILDINGS_ADMIN");

		assertThat(EffectivePermissionEvaluator.hasPermission(codes, "BUILDINGS_VIEW")).isTrue();
		assertThat(EffectivePermissionEvaluator.hasPermission(codes, "BUILDINGS_CREATE")).isTrue();
		assertThat(EffectivePermissionEvaluator.hasPermission(codes, "BUILDINGS_EDIT")).isTrue();
		assertThat(EffectivePermissionEvaluator.hasPermission(codes, "BUILDINGS_DELETE")).isTrue();
		assertThat(EffectivePermissionEvaluator.hasPermission(codes, "BUILDINGS_ADMIN")).isTrue();
	}

	@Test
	void moduleAdmin_doesNotAuthorizeOtherModules() {
		Set<String> codes = Set.of("BUILDINGS_ADMIN");

		assertThat(EffectivePermissionEvaluator.hasPermission(codes, "SCHEDULE_VIEW")).isFalse();
	}

	@Test
	void hasAnyPermission_returnsTrueWhenOneMatches() {
		Set<String> codes = Set.of("BUILDINGS_VIEW");

		assertThat(EffectivePermissionEvaluator.hasAnyPermission(codes, "BUILDINGS_VIEW", "BUILDINGS_ADMIN"))
				.isTrue();
	}

	@Test
	void hasAllPermissions_requiresEveryPermission() {
		Set<String> codes = Set.of("BUILDINGS_VIEW", "BUILDINGS_EDIT");

		assertThat(EffectivePermissionEvaluator.hasAllPermissions(codes, "BUILDINGS_VIEW", "BUILDINGS_EDIT"))
				.isTrue();
		assertThat(EffectivePermissionEvaluator.hasAllPermissions(codes, "BUILDINGS_VIEW", "BUILDINGS_CREATE"))
				.isFalse();
	}

	@Test
	void hasModuleAdmin_detectsAdminPermission() {
		Set<String> codes = Set.of("BUILDINGS_ADMIN");

		assertThat(EffectivePermissionEvaluator.hasModuleAdmin(codes, "BUILDINGS")).isTrue();
		assertThat(EffectivePermissionEvaluator.hasModuleAdmin(codes, "SCHEDULE")).isFalse();
	}
}
