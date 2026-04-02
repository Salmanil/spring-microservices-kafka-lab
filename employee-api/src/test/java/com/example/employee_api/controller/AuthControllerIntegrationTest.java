package com.example.employee_api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.test.web.servlet.MockMvc;

import com.example.employee_api.model.AuthResponse;
import com.example.employee_api.model.RegisterRequest;
import com.example.employee_api.repository.AppUserRepository;
import com.example.employee_api.service.AuthService;
import com.example.employee_api.service.EmployeeService;
import com.example.employee_api.service.KafkaProducerService;
import com.example.employee_api.service.NotificationResilienceService;
import com.example.employee_api.service.RedisDistributedLockService;
import com.example.employee_api.service.RedisLearningService;
import com.example.employee_api.service.RedisPubSubSubscriber;
import com.example.employee_api.service.RedisPubSubStore;
import com.example.employee_api.service.RedisStreamsService;
import com.example.employee_api.service.TokenBlacklistService;
import com.example.employee_api.security.AppUserDetailsService;
import com.example.employee_api.security.JwtAuthenticationFilter;
import com.example.employee_api.security.JwtService;

@WebMvcTest(AuthController.class)
@Import(ApiExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private EmployeeService employeeService;

    @MockBean
    private KafkaProducerService kafkaProducerService;

    @MockBean
    private NotificationResilienceService notificationResilienceService;

    @MockBean
    private RedisLearningService redisLearningService;

    @MockBean
    private RedisConnectionFactory redisConnectionFactory;

    @MockBean
    private RedisMessageListenerContainer redisMessageListenerContainer;

    @MockBean
    private RedisPubSubSubscriber redisPubSubSubscriber;

    @MockBean
    private RedisPubSubStore redisPubSubStore;

    @MockBean
    private RedisStreamsService redisStreamsService;

    @MockBean
    private RedisDistributedLockService redisDistributedLockService;

    @MockBean
    private AppUserRepository appUserRepository;

    @MockBean
    private AppUserDetailsService appUserDetailsService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private TokenBlacklistService tokenBlacklistService;

    @Test
    void registerShouldReturnTokensForValidRequest() throws Exception {
        AuthResponse response = new AuthResponse();
        response.setTokenType("Bearer");
        response.setAccessToken("access-token");
        response.setRefreshToken("refresh-token");
        response.setUsername("salmauser");
        response.setAccessTokenExpiresAtEpochSeconds(1774500000L);
        response.setRefreshTokenExpiresAtEpochSeconds(1775100000L);

        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "salmauser",
                                  "password": "Password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.username").value("salmauser"));

        verify(authService).register(any(RegisterRequest.class));
    }

    @Test
    void registerShouldRejectInvalidPayload() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "",
                                  "password": "short"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.fieldErrors.username").exists())
                .andExpect(jsonPath("$.fieldErrors.password").exists());
    }
}
