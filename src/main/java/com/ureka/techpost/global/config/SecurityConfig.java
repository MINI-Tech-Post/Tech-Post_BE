package com.ureka.techpost.global.config;

import com.ureka.techpost.domain.auth.handler.CustomLogoutHandler;
import com.ureka.techpost.domain.auth.jwt.JwtAuthenticationFilter;
import com.ureka.techpost.domain.auth.jwt.JwtUtil;
import com.ureka.techpost.domain.auth.service.TokenService;
import com.ureka.techpost.domain.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtUtil jwtUtil;
	private final UserRepository userRepository;
	private final TokenService tokenService;
	private final CustomLogoutHandler customLogoutHandler;
//	private final CustomOAuth2UserService customOAuth2UserService;
//	private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

	static final String[] WHITE_LIST = {"/swagger-ui/**",
			"/v3/api-docs/**",
			"/swagger-resources/**",
			"/health",
			"/", "/login", "/signup", "/css/**", "/js/**", "/oauth2/**",
			"/api/auth/**"
	};

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
		return configuration.getAuthenticationManager();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(Collections.singletonList("http://localhost:5173"));
		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(Collections.singletonList("*"));
		configuration.setAllowCredentials(true);
		configuration.setExposedHeaders(Collections.singletonList("Authorization"));
		configuration.setMaxAge(3600L);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

		http
				.cors(cors -> cors.configurationSource(corsConfigurationSource()))
				.csrf(AbstractHttpConfigurer::disable)
				.formLogin(AbstractHttpConfigurer::disable)
				.httpBasic(AbstractHttpConfigurer::disable)
				.sessionManagement(session -> session
						.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

				.authorizeHttpRequests(auth -> auth
						.requestMatchers(WHITE_LIST).permitAll()
						.anyRequest().permitAll()
				)

				.logout(logout -> logout
						.logoutUrl("/api/auth/logout")
						.addLogoutHandler(customLogoutHandler)
						.logoutSuccessHandler((request, response, authentication) -> {
							response.setStatus(HttpServletResponse.SC_OK);
						}))

				.addFilterBefore(new JwtAuthenticationFilter(jwtUtil, userRepository, tokenService), UsernamePasswordAuthenticationFilter.class);


		return http.build();
	}
}
