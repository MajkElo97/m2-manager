package pl.m2manager.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import pl.m2manager.security.auth.AccountPasswordService;
import pl.m2manager.security.auth.MustChangePasswordFilter;
import pl.m2manager.security.jwt.JwtAuthenticationFilter;
import pl.m2manager.security.jwt.JwtService;

/**
 * Development security configuration.
 * Parses JWT bearer tokens when present so {@code @PreAuthorize} works after login,
 * while keeping CSRF disabled and permitting all HTTP requests for local development.
 */
@Configuration
@Profile("dev")
public class DevSecurityConfig {

	@Bean
	JwtAuthenticationFilter jwtAuthenticationFilter(JwtService jwtService) {
		return new JwtAuthenticationFilter(jwtService);
	}

	@Bean
	MustChangePasswordFilter mustChangePasswordFilter(AccountPasswordService accountPasswordService) {
		return new MustChangePasswordFilter(accountPasswordService);
	}

	@Bean
	SecurityFilterChain devSecurityFilterChain(
			HttpSecurity http,
			JwtAuthenticationFilter jwtAuthenticationFilter,
			MustChangePasswordFilter mustChangePasswordFilter
	) throws Exception {
		http
				.csrf(AbstractHttpConfigurer::disable)
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
				.addFilterAfter(mustChangePasswordFilter, JwtAuthenticationFilter.class)
				.httpBasic(AbstractHttpConfigurer::disable)
				.formLogin(AbstractHttpConfigurer::disable);

		return http.build();
	}
}
