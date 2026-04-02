package com.example.employee_api.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RequestAuditInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RequestAuditInterceptor.class);

    private static final String START_TIME_ATTRIBUTE = "requestAuditInterceptor.startTime";
    private static final String INTERCEPTOR_NAME = "employee-api-request-interceptor";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_TIME_ATTRIBUTE, System.currentTimeMillis());
        response.setHeader("X-Intercepted-By", INTERCEPTOR_NAME);
        log.info("interceptor preHandle method={} uri={}", request.getMethod(), request.getRequestURI());
        return true;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception ex) {
        Object startTimeValue = request.getAttribute(START_TIME_ATTRIBUTE);
        long durationMs = startTimeValue instanceof Long startTime
                ? System.currentTimeMillis() - startTime
                : -1L;
        log.info("interceptor afterCompletion method={} uri={} status={} durationMs={}",
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus(),
                durationMs);
    }
}
