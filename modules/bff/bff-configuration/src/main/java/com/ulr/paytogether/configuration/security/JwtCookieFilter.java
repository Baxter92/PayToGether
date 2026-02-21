package com.ulr.paytogether.configuration.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

/**
 * Filtre pour extraire le JWT des cookies si pas présent dans le header Authorization
 */
@Component
@Slf4j
public class JwtCookieFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) 
            throws ServletException, IOException {
        
        // Vérifier si le header Authorization est déjà présent
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // Chercher le token dans les cookies
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                Arrays.stream(cookies)
                        .filter(cookie -> "access_token".equals(cookie.getName()))
                        .findFirst()
                        .ifPresent(cookie -> {
                            log.debug("Token JWT trouvé dans les cookies");
                            // Ajouter le token dans le header pour Spring Security
                            request.setAttribute("Authorization", "Bearer " + cookie.getValue());
                        });
            }
        }
        
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Ne pas filtrer les endpoints publics
        return path.startsWith("/api/public/") || path.startsWith("/api/auth/login");
    }
}
