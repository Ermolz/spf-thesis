package com.example.freelance.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@Order(1)
public class MdcFilter extends OncePerRequestFilter {

    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String TRACE_ID_MDC_KEY = "traceId";
    private static final String USER_ID_MDC_KEY = "userId";
    private static final String REQUEST_PATH_MDC_KEY = "requestPath";
    private static final String REQUEST_METHOD_MDC_KEY = "requestMethod";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        try {
            String traceId = request.getHeader(TRACE_ID_HEADER);
            if (traceId == null || traceId.isEmpty()) {
                traceId = UUID.randomUUID().toString();
            }
            
            MDC.put(TRACE_ID_MDC_KEY, traceId);
            MDC.put(REQUEST_PATH_MDC_KEY, request.getRequestURI());
            MDC.put(REQUEST_METHOD_MDC_KEY, request.getMethod());
            
            response.setHeader(TRACE_ID_HEADER, traceId);
            
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}

