package pl.m2manager.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import pl.m2manager.security.jwt.JwtAuthenticationEntryPoint;
import pl.m2manager.security.jwt.JwtAuthenticationFilter;
import pl.m2manager.security.jwt.JwtService;

/**
 * Production security configuration with JWT bearer authentication.
 */
@Configuration
@Profile("!dev & !test")
public class SecurityConfig {

	@Bean
	JwtAuthenticationFilter jwtAuthenticationFilter(JwtService jwtService) {
		return new JwtAuthenticationFilter(jwtService);
	}

	@Bean
	SecurityFilterChain securityFilterChain(
			HttpSecurity http,
			JwtAuthenticationFilter jwtAuthenticationFilter,
			JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint
	) throws Exception {
		http
				.csrf(AbstractHttpConfigurer::disable)
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
						.requestMatchers("/api/**").authenticated()
						.anyRequest().denyAll()
				)
				.exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
				.httpBasic(AbstractHttpConfigurer::disable)
				.formLogin(AbstractHttpConfigurer::disable);

		return http.build();
	}
}
