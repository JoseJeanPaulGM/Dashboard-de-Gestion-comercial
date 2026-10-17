package com.gestioncomercial.api.identity.infrastructure;

import com.gestioncomercial.api.identity.application.IdentityStore;
import com.gestioncomercial.api.identity.application.IdentityUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtDecoder decoder;
    private final IdentityStore store;
    private final SecurityProblemWriter errors;
    private final Clock clock;

    public JwtAuthenticationFilter(JwtDecoder decoder, IdentityStore store, SecurityProblemWriter errors, Clock clock) {
        this.decoder = decoder;
        this.store = store;
        this.errors = errors;
        this.clock = clock;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header == null) {
            chain.doFilter(request, response);
            return;
        }
        if (!header.startsWith("Bearer ")) {
            errors.write(request, response, 401, "Authentication required", "UNAUTHENTICATED", "A valid Bearer token is required.");
            return;
        }
        long id;
        List<SimpleGrantedAuthority> authorities;
        try {
            Jwt jwt = decoder.decode(header.substring(7));
            Instant now = Instant.now(clock);
            if (!"gestion-comercial".equals(jwt.getClaimAsString("iss"))) {
                throw new IllegalArgumentException("Invalid issuer");
            }
            if (jwt.getExpiresAt() == null || !jwt.getExpiresAt().isAfter(now)
                    || jwt.getIssuedAt() == null || jwt.getIssuedAt().isAfter(now)
                    || jwt.getSubject() == null || !jwt.getSubject().matches("[1-9][0-9]*")) {
                throw new IllegalArgumentException("Incomplete or expired token");
            }
            id = Long.parseLong(jwt.getSubject());
            IdentityUser user = store.findById(id).orElseThrow();
            Object version = jwt.getClaim("version");
            if (!user.active() || !(version instanceof Long || version instanceof Integer)
                    || ((Number) version).longValue() != user.tokenVersion()) {
                throw new IllegalArgumentException("Invalid account or token version");
            }
            authorities = user.permissions().stream()
                    .map(SimpleGrantedAuthority::new).toList();
        } catch (JwtException | IllegalArgumentException | java.util.NoSuchElementException exception) {
            SecurityContextHolder.clearContext();
            errors.write(request, response, 401, "Authentication required", "UNAUTHENTICATED", "A valid Bearer token is required.");
            return;
        }
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(Long.toString(id), null, authorities));
        try {
            chain.doFilter(request, response);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
