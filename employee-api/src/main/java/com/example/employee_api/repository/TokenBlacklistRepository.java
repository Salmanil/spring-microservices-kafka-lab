package com.example.employee_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.employee_api.entity.TokenBlacklistEntry;

public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklistEntry, Integer> {

    boolean existsByToken(String token);
}
