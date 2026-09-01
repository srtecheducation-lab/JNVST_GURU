package com.jnvstguru.jnvst_guru_backend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.startsWith("/api/v1");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        String method = request.getMethod();
        String path = request.getRequestURI();
        long startTime = System.currentTimeMillis();

        MDC.put("requestId", requestId);

        try {
            if (!"/api/v1/health".equals(path)) {
                log.info("[REQUEST] {} {}", method, path);
                String authHeader = request.getHeader("Authorization");
                log.info("[REQUEST] Authorization header present={} value={}", authHeader != null, authHeader != null ? authHeader.substring(0, Math.min(30, authHeader.length())) + (authHeader.length() > 30 ? "..." : "") : "null");
                log.info("[REQUEST] Host={} Origin={} User-Agent={}", request.getHeader("Host"), request.getHeader("Origin"), request.getHeader("User-Agent"));
            }
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            int status = response.getStatus();

            if ("/api/v1/health".equals(path)) {
                log.debug("[RESPONSE] {} {} -> {} ({} ms)", method, path, status, duration);
            } else {
                log.info("[RESPONSE] {} {} -> {} ({} ms)", method, path, status, duration);
            }

            MDC.remove("requestId");
        }
    }
}
