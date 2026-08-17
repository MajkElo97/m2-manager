package pl.m2manager.security.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.m2manager.security.PasswordEncoderConfig;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = PasswordEncoderConfig.class)
class PasswordEncoderTest {

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Test
	void encodedPassword_matchesRawPassword() {
		String rawPassword = "passwordA";

		String encodedPassword = passwordEncoder.encode(rawPassword);

		assertThat(passwordEncoder.matches(rawPassword, encodedPassword)).isTrue();
	}

	@Test
	void encodedPassword_doesNotMatchDifferentPassword() {
		String encodedPassword = passwordEncoder.encode("passwordA");

		assertThat(passwordEncoder.matches("passwordB", encodedPassword)).isFalse();
	}

	@Test
	void encodedPassword_isNotEqualToRawPassword() {
		String rawPassword = "passwordA";

		String encodedPassword = passwordEncoder.encode(rawPassword);

		assertThat(encodedPassword).isNotEqualTo(rawPassword);
	}
}
