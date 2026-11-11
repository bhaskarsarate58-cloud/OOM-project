package com.cineverse.service;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Locale;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cineverse.dto.CustomerProfileDto;
import com.cineverse.dto.LoginRequest;
import com.cineverse.dto.RegisterRequest;
import com.cineverse.entity.CustomerAccount;
import com.cineverse.exception.InvalidRequestException;
import com.cineverse.exception.RequestConflictException;
import com.cineverse.repository.CustomerAccountRepository;

@Service
public class CustomerAccountService {

    private final CustomerAccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomerAccountService(CustomerAccountRepository accountRepository, PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public CustomerProfileDto register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (accountRepository.existsByEmail(email)) {
            throw new RequestConflictException("An account with this email already exists");
        }
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new InvalidRequestException(
                    "Request validation failed",
                    Map.of("password", "Password must be 72 bytes or fewer"));
        }
        String displayName = request.displayName().trim();
        if (displayName.isEmpty()) {
            throw new InvalidRequestException("Request validation failed", Map.of("displayName", "Name is required"));
        }
        CustomerAccount account = accountRepository.save(new CustomerAccount(
                email,
                displayName,
                passwordEncoder.encode(request.password())));
        return CustomerProfileDto.from(account);
    }

    @Transactional(readOnly = true)
    public Optional<CustomerProfileDto> authenticate(LoginRequest request) {
        String email = normalizeEmail(request.email());
        return accountRepository.findByEmail(email)
                .filter(account -> request.password().getBytes(StandardCharsets.UTF_8).length <= 72)
                .filter(account -> passwordEncoder.matches(request.password(), account.getPasswordHash()))
                .map(CustomerProfileDto::from);
    }

    @Transactional(readOnly = true)
    public Optional<CustomerProfileDto> findProfile(long accountId) {
        return accountRepository.findById(accountId).map(CustomerProfileDto::from);
    }

    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
