package com.example.employee_api.service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.employee_api.entity.AppUser;
import com.example.employee_api.entity.Role;
import com.example.employee_api.model.AuthRequest;
import com.example.employee_api.model.AuthResponse;
import com.example.employee_api.model.LogoutRequest;
import com.example.employee_api.model.RefreshTokenRequest;
import com.example.employee_api.model.RegisterRequest;
import com.example.employee_api.repository.AppUserRepository;
import com.example.employee_api.repository.RoleRepository;
import com.example.employee_api.security.JwtService;

@Service
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;
    private final String defaultRole;

    public AuthService(
            AppUserRepository appUserRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            TokenBlacklistService tokenBlacklistService,
            @Value("${app.security.default-role}") String defaultRole) {
        this.appUserRepository = appUserRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.tokenBlacklistService = tokenBlacklistService;
        this.defaultRole = defaultRole;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (appUserRepository.existsByUsername(request.getUsername())) {
            throw new AuthenticationException("Username already exists");
        }

        Role role = roleRepository.findByName(defaultRole)
                .orElseThrow(() -> new AuthenticationException(
                        "Default role '" + defaultRole + "' is missing in the roles table"));

        AppUser appUser = new AppUser();
        appUser.setId(nextUserId());
        appUser.setUsername(request.getUsername());
        appUser.setPassword(passwordEncoder.encode(request.getPassword()));
        appUser.setEnabled(true);
        appUser.setRoles(Set.of(role));
        appUserRepository.save(appUser);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        return toAuthResponse(authentication);
    }

    public AuthResponse login(AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        return toAuthResponse(authentication);
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        if (tokenBlacklistService.isBlacklisted(refreshToken)) {
            throw new AuthenticationException("Refresh token has been blacklisted");
        }
        if (!jwtService.isRefreshToken(refreshToken)) {
            throw new AuthenticationException("Provided token is not a refresh token");
        }

        String username = jwtService.extractUsername(refreshToken);
        AppUser appUser = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new AuthenticationException("User not found for refresh token"));
        if (!jwtService.isTokenValid(refreshToken, username)) {
            throw new AuthenticationException("Refresh token is expired or invalid");
        }

        tokenBlacklistService.blacklist(refreshToken, jwtService.extractExpiry(refreshToken));
        return buildAuthResponse(
                username,
                appUser.getRoles().stream().map(Role::getName).toList());
    }

    @Transactional
    public void logout(LogoutRequest request) {
        String refreshToken = request.getRefreshToken();
        if (!jwtService.isRefreshToken(refreshToken)) {
            throw new AuthenticationException("Provided token is not a refresh token");
        }
        tokenBlacklistService.blacklist(refreshToken, jwtService.extractExpiry(refreshToken));
    }

    private AuthResponse toAuthResponse(Authentication authentication) {
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        return buildAuthResponse(authentication.getName(), roles);
    }

    private AuthResponse buildAuthResponse(String username, List<String> roles) {
        String accessToken = jwtService.generateAccessToken(
                username,
                roles.stream().map(role -> (GrantedAuthority) () -> role).toList());
        String refreshToken = jwtService.generateRefreshToken(username);

        AuthResponse response = new AuthResponse();
        response.setTokenType("Bearer");
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setUsername(username);
        response.setRoles(roles);
        response.setAccessTokenExpiresAtEpochSeconds(jwtService.extractExpiry(accessToken).getEpochSecond());
        response.setRefreshTokenExpiresAtEpochSeconds(jwtService.extractExpiry(refreshToken).getEpochSecond());
        return response;
    }

    private Integer nextUserId() {
        return appUserRepository.findAll().stream()
                .map(AppUser::getId)
                .max(Comparator.naturalOrder())
                .orElse(0) + 1;
    }
}
