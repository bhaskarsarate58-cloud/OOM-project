package com.cineverse.config;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.cineverse.entity.CustomerAccount;
import com.cineverse.entity.AccountRole;
import com.cineverse.repository.CustomerAccountRepository;

@Component
public class DemoCustomerDataLoader implements ApplicationRunner {

    public static final String DEMO_EMAIL = "demo@cineverse.local";
    private final CustomerAccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final String demoPassword;
    private final String adminEmail;
    private final String adminPassword;

    public DemoCustomerDataLoader(
            CustomerAccountRepository accountRepository,
            PasswordEncoder passwordEncoder,
            @Value("${cineverse.demo-account.password:MovieNight2026!}") String demoPassword,
            @Value("${cineverse.admin.email:}") String adminEmail,
            @Value("${cineverse.admin.password:}") String adminPassword) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.demoPassword = demoPassword;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        String email = DEMO_EMAIL.toLowerCase(Locale.ROOT);
        if (!accountRepository.existsByEmail(email)) {
            accountRepository.save(new CustomerAccount(
                    email,
                    "CineVerse Demo",
                    passwordEncoder.encode(demoPassword)));
        }
        if (adminEmail.isBlank() && adminPassword.isBlank()) return;
        if (adminEmail.isBlank() || adminPassword.isBlank()) {
            throw new IllegalStateException(
                    "Set both CINEVERSE_ADMIN_EMAIL and CINEVERSE_ADMIN_PASSWORD to configure admin access");
        }
        if (adminPassword.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) {
            throw new IllegalStateException("CINEVERSE_ADMIN_PASSWORD must be 72 bytes or fewer");
        }
        String configuredEmail = adminEmail.trim().toLowerCase(Locale.ROOT);
        var existingAdmin = accountRepository.findByEmail(configuredEmail);
        if (existingAdmin.isPresent() && existingAdmin.get().getRole() != AccountRole.ADMIN) {
            throw new IllegalStateException("Configured admin email belongs to a non-admin account");
        }
        if (existingAdmin.isEmpty()) {
            accountRepository.save(new CustomerAccount(
                    configuredEmail,
                    "CineVerse Admin",
                    passwordEncoder.encode(adminPassword),
                    AccountRole.ADMIN));
        }
    }
}
