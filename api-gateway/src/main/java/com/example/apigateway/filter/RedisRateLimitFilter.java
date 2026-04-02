package com.example.apigateway.filter;

import java.io.IOException;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RedisRateLimitFilter extends OncePerRequestFilter {

    private final StringRedisTemplate stringRedisTemplate;
    private final int maxRequests;
    private final long windowSeconds;

    public RedisRateLimitFilter(
            StringRedisTemplate stringRedisTemplate,
            @Value("${app.rate-limit.employee.max-requests:5}") int maxRequests,
            @Value("${app.rate-limit.employee.window-seconds:60}") long windowSeconds) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.maxRequests = maxRequests;
        this.windowSeconds = windowSeconds;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getServletPath().startsWith("/api/employees");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String clientId = request.getHeader("X-Client-Id");
        if (clientId == null || clientId.isBlank()) {
            clientId = request.getRemoteAddr();
        }

        String key = "rate-limit:employees:" + clientId;
        Long currentCount = stringRedisTemplate.opsForValue().increment(key);
        if (currentCount != null && currentCount == 1L) {
            stringRedisTemplate.expire(key, Duration.ofSeconds(windowSeconds));
        }

        Long ttl = stringRedisTemplate.getExpire(key);
        response.setHeader("X-RateLimit-Limit", String.valueOf(maxRequests));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(Math.max(0, maxRequests - (currentCount == null ? 0 : currentCount.intValue()))));
        response.setHeader("X-RateLimit-Reset-Seconds", String.valueOf(ttl == null ? -1 : ttl));

        if (currentCount != null && currentCount > maxRequests) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("""
                    {
                      "error": "Too Many Requests",
                      "message": "Redis rate limit exceeded for /api/employees. Wait for the window to reset or use a different X-Client-Id."
                    }
                    """);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
