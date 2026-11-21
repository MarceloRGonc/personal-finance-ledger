package com.finances.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
class SameOriginFilter extends OncePerRequestFilter {

    private static final Set<String> READ_ONLY_METHODS = Set.of("GET", "HEAD", "OPTIONS", "TRACE");
    private static final Set<String> TRUSTED_FETCH_SITES = Set.of("same-origin", "none");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (READ_ONLY_METHODS.contains(request.getMethod()) || comesFromThisApp(request)) {
            chain.doFilter(request, response);
            return;
        }
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.TEXT_PLAIN_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("Cross-site request blocked");
    }

    private static boolean comesFromThisApp(HttpServletRequest request) {
        var fetchSite = request.getHeader("Sec-Fetch-Site");
        if (fetchSite != null) {
            return TRUSTED_FETCH_SITES.contains(fetchSite);
        }
        var origin = request.getHeader(HttpHeaders.ORIGIN);
        return origin == null || originMatchesHost(origin, request.getHeader(HttpHeaders.HOST));
    }

    private static boolean originMatchesHost(String origin, String host) {
        try {
            return host != null && host.equals(URI.create(origin).getAuthority());
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
