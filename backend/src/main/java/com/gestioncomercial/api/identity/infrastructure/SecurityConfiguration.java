package com.gestioncomercial.api.identity.infrastructure;

import jakarta.servlet.DispatcherType;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfiguration {
    @Bean
    FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration(JwtAuthenticationFilter filter) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwt,
                                            SecurityProblemWriter errors) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(handler -> handler
                        .authenticationEntryPoint((request, response, exception) ->
                                errors.write(request, response, 401, "Authentication required", "UNAUTHENTICATED", "A valid Bearer token is required."))
                        .accessDeniedHandler((request, response, exception) ->
                                errors.write(request, response, 403, "Access denied", "FORBIDDEN", "Permission is required.")))
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers("/actuator/health", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login", "/api/v1/auth/refresh", "/api/v1/auth/logout").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/auth/me").authenticated()
                        .requestMatchers("/api/v1/users/**").hasAuthority("IDENTITY_MANAGE")
                        .requestMatchers(HttpMethod.GET, "/api/v1/customers/**").hasAuthority("CUSTOMER_READ")
                        .requestMatchers("/api/v1/customers/**").hasAuthority("CUSTOMER_WRITE")
                        .requestMatchers(HttpMethod.GET, "/api/v1/categories/**", "/api/v1/products/**").hasAuthority("CATALOG_READ")
                        .requestMatchers("/api/v1/categories/**", "/api/v1/products/**").hasAuthority("CATALOG_WRITE")
                        .requestMatchers(HttpMethod.GET, "/api/v1/suppliers/**").hasAuthority("SUPPLIER_READ")
                        .requestMatchers("/api/v1/suppliers/**").hasAuthority("SUPPLIER_WRITE")
                        .requestMatchers(HttpMethod.GET, "/api/v1/purchases/**").hasAuthority("PURCHASE_READ")
                        .requestMatchers("/api/v1/purchases/**").hasAuthority("PURCHASE_WRITE")
                        .requestMatchers(HttpMethod.GET, "/api/v1/sales/**").hasAuthority("SALE_READ")
                        .requestMatchers("/api/v1/sales/**").hasAuthority("SALE_WRITE")
                        .requestMatchers(HttpMethod.GET, "/api/v1/inventory/**").hasAuthority("INVENTORY_READ")
                        .requestMatchers("/api/v1/inventory/**").hasAuthority("INVENTORY_ADJUST")
                        .requestMatchers("/api/v1/system/**").authenticated()
                        .anyRequest().denyAll())
                .addFilterBefore(jwt, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
