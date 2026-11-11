package com.cineverse.dto;

import java.time.LocalDateTime;

import com.cineverse.entity.AccountRole;
import com.cineverse.entity.CustomerAccount;

public record CustomerProfileDto(Long id, String displayName, String email, LocalDateTime createdAt, AccountRole role) {

    public static CustomerProfileDto from(CustomerAccount account) {
        return new CustomerProfileDto(
                account.getId(),
                account.getDisplayName(),
                account.getEmail(),
                account.getCreatedAt(),
                account.getRole());
    }
}
