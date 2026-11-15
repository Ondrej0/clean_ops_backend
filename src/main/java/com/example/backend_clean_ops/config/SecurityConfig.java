package com.example.backend_clean_ops.config;

import com.example.backend_clean_ops.repository.UserRepository;
import org.springframework.context.annotation.*;
import org.springframework.web.cors.*;
import java.util.List;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import java.util.UUID;

/** Cookie-session authentication and CSRF protection for the entire API. */
@Configuration
public class SecurityConfig {
    @Bean
    PasswordEncoder employeePasswordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    UserDetailsService employeeUserDetails(UserRepository users) {
        return login -> {
            try {
                String[] parts = login.split(":", 2);
                var matches = users.findAllByTenantIdAndEmailIgnoreCaseAndActiveTrue(
                        UUID.fromString(parts[0]), parts[1].trim());
                if (matches.size() != 1) throw new UsernameNotFoundException("Invalid credentials");
                var employee = matches.getFirst();
                return User.withUsername(employee.getId().toString()).password(employee.getPasswordHash())
                        .roles(employee.getRole().name()).build();
            } catch (IllegalArgumentException | IndexOutOfBoundsException e) {
                throw new UsernameNotFoundException("Invalid credentials");
            }
        };
    }

    @Bean
    SecurityFilterChain apiSecurity(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> {})
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/session/csrf", "/api/session/login").permitAll()
                        .requestMatchers("/api/tenant", "/api/tenant/**").denyAll()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().denyAll())
                .exceptionHandling(errors -> errors.authenticationEntryPoint((request, response, exception) -> response.setStatus(401)))
                .requestCache(cache -> cache.disable())
                .formLogin(login -> login.loginProcessingUrl("/api/session/login")
                        .successHandler((request, response, authentication) -> response.setStatus(204))
                        .failureHandler((request, response, exception) -> response.setStatus(401)))
                .logout(logout -> logout.logoutUrl("/api/session/logout")
                        .logoutSuccessHandler((request, response, authentication) -> response.setStatus(204)))
                .build();
    }
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Content-Type", "Authorization", "X-CSRF-TOKEN"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}
