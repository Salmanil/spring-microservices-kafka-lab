package com.example.employee_api.model;

import java.util.List;

public class AuthResponse {

    private String tokenType;
    private String accessToken;
    private String refreshToken;
    private String username;
    private List<String> roles;
    private long accessTokenExpiresAtEpochSeconds;
    private long refreshTokenExpiresAtEpochSeconds;

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public long getAccessTokenExpiresAtEpochSeconds() {
        return accessTokenExpiresAtEpochSeconds;
    }

    public void setAccessTokenExpiresAtEpochSeconds(long accessTokenExpiresAtEpochSeconds) {
        this.accessTokenExpiresAtEpochSeconds = accessTokenExpiresAtEpochSeconds;
    }

    public long getRefreshTokenExpiresAtEpochSeconds() {
        return refreshTokenExpiresAtEpochSeconds;
    }

    public void setRefreshTokenExpiresAtEpochSeconds(long refreshTokenExpiresAtEpochSeconds) {
        this.refreshTokenExpiresAtEpochSeconds = refreshTokenExpiresAtEpochSeconds;
    }
}
