package com.cineverse.controller;

import java.time.LocalDate;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cineverse.dto.AdminDashboardDto;
import com.cineverse.dto.UpdateShowPriceRequest;
import com.cineverse.service.AdminDashboardService;
import com.cineverse.service.CsrfTokenService;
import com.cineverse.service.CustomerSession;
import com.cineverse.service.ShowService;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminDashboardService dashboardService;
    private final ShowService showService;
    private final CsrfTokenService csrfTokenService;

    public AdminController(
            AdminDashboardService dashboardService,
            ShowService showService,
            CsrfTokenService csrfTokenService) {
        this.dashboardService = dashboardService;
        this.showService = showService;
        this.csrfTokenService = csrfTokenService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> dashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            HttpServletRequest request) {
        ResponseEntity<Map<String, String>> accessFailure = adminAccessFailure(request);
        if (accessFailure != null) return accessFailure;
        if (from != null && to != null && from.isAfter(to)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Start date must not be after end date"));
        }
        AdminDashboardDto dashboard = dashboardService.getDashboard(from, to);
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/shows")
    public ResponseEntity<?> shows(HttpServletRequest request) {
        ResponseEntity<Map<String, String>> accessFailure = adminAccessFailure(request);
        if (accessFailure != null) return accessFailure;
        return ResponseEntity.ok(showService.getAllShows());
    }

    @PatchMapping("/shows/{showId}/price")
    public ResponseEntity<?> updateShowPrice(
            @PathVariable @Positive Long showId,
            @Valid @RequestBody UpdateShowPriceRequest requestBody,
            @RequestHeader(value = "X-CSRF-Token", required = false) String suppliedToken,
            HttpServletRequest request) {
        if (!csrfTokenService.isValid(request.getSession(false), suppliedToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Session verification failed. Refresh and try again."));
        }
        ResponseEntity<Map<String, String>> accessFailure = adminAccessFailure(request);
        if (accessFailure != null) return accessFailure;
        return ResponseEntity.ok(showService.updateTicketPrice(showId, requestBody.ticketPrice()));
    }

    private ResponseEntity<Map<String, String>> adminAccessFailure(HttpServletRequest request) {
        Long accountId = CustomerSession.accountId(request.getSession(false));
        if (accountId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Please log in"));
        }
        if (!dashboardService.isAdmin(accountId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Admin access is required"));
        }
        return null;
    }
}
