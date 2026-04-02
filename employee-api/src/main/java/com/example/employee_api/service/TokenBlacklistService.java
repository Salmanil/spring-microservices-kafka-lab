package com.example.employee_api.service;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.employee_api.entity.TokenBlacklistEntry;
import com.example.employee_api.repository.TokenBlacklistRepository;

@Service
public class TokenBlacklistService {

    private final TokenBlacklistRepository tokenBlacklistRepository;

    public TokenBlacklistService(TokenBlacklistRepository tokenBlacklistRepository) {
        this.tokenBlacklistRepository = tokenBlacklistRepository;
    }

    public boolean isBlacklisted(String token) {
        return tokenBlacklistRepository.existsByToken(token);
    }

    @Transactional
    public void blacklist(String token, Instant expiryDate) {
        if (!tokenBlacklistRepository.existsByToken(token)) {
            tokenBlacklistRepository.save(new TokenBlacklistEntry(token, expiryDate));
        }
    }
}
