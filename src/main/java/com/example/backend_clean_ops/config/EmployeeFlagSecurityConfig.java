package com.example.backend_clean_ops.config;

import com.example.backend_clean_ops.repository.UserRepository;
import org.springframework.context.annotation.*;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import java.util.UUID;

/** Session authentication for employee reporting; legacy APIs retain their existing policy. */
@Configuration
public class EmployeeFlagSecurityConfig {
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

    @Bean @Order(1)
    SecurityFilterChain employeeFlagSecurity(HttpSecurity http) throws Exception {
        return http.securityMatcher("/api/employee-flags/**", "/api/me/**", "/api/session/**", "/api/cleaner/**")
                .cors(cors -> {})
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/cleaner/**").permitAll()
                        .requestMatchers("/api/session/csrf", "/api/session/login").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(errors -> errors.authenticationEntryPoint((request, response, exception) -> response.setStatus(401)))
                .requestCache(cache -> cache.disable())
                .formLogin(login -> login.loginProcessingUrl("/api/session/login")
                        .successHandler((request, response, authentication) -> response.setStatus(204))
                        .failureHandler((request, response, exception) -> response.setStatus(401)))
                .logout(logout -> logout.logoutUrl("/api/session/logout")
                        .logoutSuccessHandler((request, response, authentication) -> response.setStatus(204)))
                .build();
    }
}
