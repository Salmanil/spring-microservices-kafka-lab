package com.example.employee_api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.example.employee_api.interceptor.RequestAuditInterceptor;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final RequestAuditInterceptor requestAuditInterceptor;

    public WebMvcConfig(RequestAuditInterceptor requestAuditInterceptor) {
        this.requestAuditInterceptor = requestAuditInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestAuditInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/actuator/**");
    }
}
