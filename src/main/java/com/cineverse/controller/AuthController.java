package com.cineverse.controller;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cineverse.dto.CsrfTokenDto;
import com.cineverse.dto.CustomerProfileDto;
import com.cineverse.dto.LoginRequest;
import com.cineverse.dto.RegisterRequest;
import com.cineverse.entity.AccountRole;
import com.cineverse.service.BookingService;
import com.cineverse.service.CsrfTokenService;
import com.cineverse.service.CustomerAccountService;
import com.cineverse.service.CustomerSession;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final CustomerAccountService accountService;
    private final BookingService bookingService;
    private final CsrfTokenService csrfTokenService;
    private final String adminId;

    public AuthController(
            CustomerAccountService accountService,
            BookingService bookingService,
            CsrfTokenService csrfTokenService,
            @Value("${cineverse.admin.id:}") String adminId) {
        this.accountService = accountService;
        this.bookingService = bookingService;
        this.csrfTokenService = csrfTokenService;
        this.adminId = adminId == null ? "" : adminId;
    }

    @GetMapping("/csrf")
    public CsrfTokenDto csrfToken(HttpSession session) {
        return new CsrfTokenDto(csrfTokenService.getOrCreate(session));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @Valid @RequestBody RegisterRequest request,
            @RequestHeader(value = "X-CSRF-Token", required = false) String suppliedToken,
            HttpServletRequest servletRequest) {
        HttpSession session = servletRequest.getSession();
        if (!csrfTokenService.isValid(session, suppliedToken)) return csrfRejected();
        CustomerProfileDto profile = accountService.register(request);
        servletRequest.changeSessionId();
        session.setAttribute(CustomerSession.ACCOUNT_ID_ATTRIBUTE, profile.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(profile);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest request,
            @RequestHeader(value = "X-CSRF-Token", required = false) String suppliedToken,
            HttpServletRequest servletRequest) {
        HttpSession session = servletRequest.getSession();
        if (!csrfTokenService.isValid(session, suppliedToken)) return csrfRejected();
        var profile = accountService.authenticate(request);
        if (profile.isEmpty() || profile.get().role() != AccountRole.CUSTOMER) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Email or password is incorrect"));
        }
        servletRequest.changeSessionId();
        session.setAttribute(CustomerSession.ACCOUNT_ID_ATTRIBUTE, profile.get().id());
        return ResponseEntity.ok(profile.get());
    }

    @PostMapping("/admin/login")
    public ResponseEntity<?> adminLogin(
            @Valid @RequestBody LoginRequest request,
            @RequestHeader(value = "X-CSRF-Token", required = false) String suppliedToken,
            HttpServletRequest servletRequest) {
        HttpSession session = servletRequest.getSession();
        if (!csrfTokenService.isValid(session, suppliedToken)) return csrfRejected();
        if (!validAdminId(request.adminId())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Admin credentials are incorrect"));
        }
        var profile = accountService.authenticate(request);
        if (profile.isEmpty() || profile.get().role() != AccountRole.ADMIN) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Admin credentials are incorrect"));
        }
        servletRequest.changeSessionId();
        session.setAttribute(CustomerSession.ACCOUNT_ID_ATTRIBUTE, profile.get().id());
        return ResponseEntity.ok(profile.get());
    }

    @GetMapping("/me")
    public ResponseEntity<?> profile(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Long id = CustomerSession.accountId(session);
        if (id == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Please log in"));
        }
        var profile = accountService.findProfile(id);
        if (profile.isEmpty()) {
            session.invalidate();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Please log in again"));
        }
        return ResponseEntity.ok(profile.get());
    }

    @GetMapping("/me/bookings")
    public ResponseEntity<?> customerBookings(HttpServletRequest request) {
        Long accountId = CustomerSession.accountId(request.getSession(false));
        if (accountId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Please log in"));
        }
        return ResponseEntity.ok(bookingService.getBookingsForCustomer(accountId));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @RequestHeader(value = "X-CSRF-Token", required = false) String suppliedToken,
            HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || !csrfTokenService.isValid(session, suppliedToken)) return csrfRejected();
        session.invalidate();
        return ResponseEntity.ok(Map.of("loggedOut", true));
    }

    private boolean validAdminId(String suppliedAdminId) {
        return !adminId.isBlank()
                && suppliedAdminId != null
                && MessageDigest.isEqual(
                        adminId.getBytes(StandardCharsets.UTF_8),
                        suppliedAdminId.getBytes(StandardCharsets.UTF_8));
    }

    private static ResponseEntity<Map<String, String>> csrfRejected() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("message", "Session verification failed. Refresh and try again."));
    }
}
