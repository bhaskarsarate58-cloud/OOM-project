package com.cineverse.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cineverse.entity.CustomerAccount;

public interface CustomerAccountRepository extends JpaRepository<CustomerAccount, Long> {

    Optional<CustomerAccount> findByEmail(String email);

    boolean existsByEmail(String email);
}
