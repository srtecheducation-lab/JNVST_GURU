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
import org.springframework.core.annotation.Order;
import org.springframework.core.Ordered;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @jakarta.annotation.PostConstruct
    public void logFilterRegistered() {
        log.info("[REQUEST] RequestLoggingFilter registered");
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return false;
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

    log.info("[REQUEST] method={} uri={} query={} remoteAddress={} contentType={} contentLength={}",
            method, request.getRequestURL(), request.getQueryString(),
            request.getRemoteAddr(), request.getContentType(), request.getContentLengthLong());

    Collections.list(request.getHeaderNames()).forEach(headerName ->
            log.info("[REQUEST_HEADER] {}={}", headerName, request.getHeader(headerName))
    );

    logAuthorizationDiagnostics(request.getHeader("Authorization"));

    filterChain.doFilter(request, response);

    }finally {
            long duration = System.currentTimeMillis() - startTime;
            int status = response.getStatus();

            log.info("[RESPONSE] {} {} -> {} ({} ms)", method, path, status, duration);

            MDC.remove("requestId");
        }
    }

    private String safeHeaderValue(String headerName, String value) {
        if (value == null) return null;
        if ("authorization".equalsIgnoreCase(headerName)
                || "cookie".equalsIgnoreCase(headerName)
                || "set-cookie".equalsIgnoreCase(headerName)) {
            return "[REDACTED]";
        }
        return value;
    }

    private void logAuthorizationDiagnostics(String authorization) {
        if (authorization == null) {
            log.info("[REQUEST_AUTH] present=false");
            return;
        }
        String trimmed = authorization.trim();
        boolean bearer = trimmed.regionMatches(true, 0, "Bearer ", 0, 7);
        String token = bearer ? trimmed.substring(7).trim() : "";
        long segmentCount = token.isEmpty() ? 0 : token.chars().filter(character -> character == '.').count() + 1;
        log.info("[REQUEST_AUTH] present=true bearerScheme={} valueLength={} jwtSegments={}",
                bearer, authorization.length(), segmentCount);
    }
}
