package com.cineverse.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import com.cineverse.entity.AccountRole;
import com.cineverse.entity.CustomerAccount;
import com.cineverse.repository.BookingRepository;
import com.cineverse.repository.CustomerAccountRepository;
import com.cineverse.service.CustomerSession;

@SpringBootTest(properties = "cineverse.admin.id=test-admin-id")
@AutoConfigureMockMvc
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomerAccountRepository accountRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private CustomerAccount admin;
    private CustomerAccount customer;

    @BeforeEach
    void prepareAccounts() {
        bookingRepository.deleteAll();
        accountRepository.deleteAll();
        admin = accountRepository.save(new CustomerAccount(
                "admin@test.local", "Admin Test", passwordEncoder.encode("admin-password"), AccountRole.ADMIN));
        customer = accountRepository.save(new CustomerAccount(
                "customer@test.local", "Customer Test", passwordEncoder.encode("customer-password"), AccountRole.CUSTOMER));
    }

    @Test
    void adminLoginIsSeparateAndRequiresAnAdminRole() throws Exception {
        MockHttpSession customerSession = new MockHttpSession();
        String customerToken = csrfToken(customerSession);
        mockMvc.perform(post("/api/auth/admin/login")
                        .session(customerSession)
                        .header("X-CSRF-Token", customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"customer@test.local\",\"password\":\"customer-password\",\"adminId\":\"test-admin-id\"}"))
                .andExpect(status().isUnauthorized());

        MockHttpSession adminSession = new MockHttpSession();
        String adminToken = csrfToken(adminSession);
        mockMvc.perform(post("/api/auth/login")
                        .session(adminSession)
                        .header("X-CSRF-Token", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@test.local\",\"password\":\"admin-password\"}"))
                .andExpect(status().isUnauthorized());

        MockHttpSession validAdminSession = new MockHttpSession();
        mockMvc.perform(post("/api/auth/admin/login")
                        .session(validAdminSession)
                        .header("X-CSRF-Token", csrfToken(validAdminSession))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@test.local\",\"password\":\"admin-password\",\"adminId\":\"wrong-id\"}"))
                .andExpect(status().isUnauthorized());

        validAdminSession = new MockHttpSession();
        mockMvc.perform(post("/api/auth/admin/login")
                        .session(validAdminSession)
                        .header("X-CSRF-Token", csrfToken(validAdminSession))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@test.local\",\"password\":\"admin-password\",\"adminId\":\"test-admin-id\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void showPricesCanOnlyBeChangedByCsrfVerifiedAdmins() throws Exception {
        mockMvc.perform(get("/api/admin/shows"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/admin/shows").session(sessionFor(customer)))
                .andExpect(status().isForbidden());

        MockHttpSession adminSession = sessionFor(admin);
        JsonNode shows = objectMapper.readTree(mockMvc.perform(get("/api/admin/shows").session(adminSession))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
        org.junit.jupiter.api.Assertions.assertTrue(shows.size() > 0);
        long showId = shows.get(0).path("id").asLong();

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch(
                        "/api/admin/shows/{showId}/price", showId)
                        .session(adminSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ticketPrice\":777.25}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch(
                        "/api/admin/shows/{showId}/price", showId)
                        .session(adminSession)
                        .header("X-CSRF-Token", csrfToken(adminSession))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ticketPrice\":777.25}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticketPrice").value(777.25));
        mockMvc.perform(get("/api/shows/{showId}", showId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticketPrice").value(777.25));

        MockHttpSession customerSession = sessionFor(customer);
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch(
                        "/api/admin/shows/{showId}/price", showId)
                        .session(customerSession)
                        .header("X-CSRF-Token", csrfToken(customerSession))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ticketPrice\":888.00}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminReportIsRoleProtectedAndFiltersBookingsByShowDate() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/admin/dashboard").session(sessionFor(customer)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Admin access is required"));

        JsonNode shows = objectMapper.readTree(mockMvc.perform(get("/api/shows"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
        JsonNode selectedShow = null;
        for (JsonNode show : shows) {
            if (show.path("status").asText().equals("SCHEDULED") &&
                    LocalDateTime.parse(show.path("startTime").asText()).isAfter(LocalDateTime.now())) {
                selectedShow = show;
                break;
            }
        }
        org.junit.jupiter.api.Assertions.assertNotNull(selectedShow);
        long showId = selectedShow.path("id").asLong();
        JsonNode seats = objectMapper.readTree(mockMvc.perform(get("/api/shows/{id}/seats", showId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
        String seatLabel = null;
        for (JsonNode seat : seats) {
            if (seat.path("available").asBoolean()) {
                seatLabel = seat.path("label").asText();
                break;
            }
        }
        org.junit.jupiter.api.Assertions.assertNotNull(seatLabel);
        MockHttpSession adminSession = sessionFor(admin);
        mockMvc.perform(post("/api/bookings")
                        .session(adminSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"showId\":" + showId + ",\"seatLabels\":[\"" + seatLabel
                                + "\"],\"paymentMethod\":\"CARD\",\"paymentOutcome\":\"SUCCESS\"}"))
                        .andExpect(status().isCreated());

        String showDate = selectedShow.path("startTime").asText().substring(0, 10);
        mockMvc.perform(get("/api/admin/dashboard")
                        .session(adminSession)
                        .param("from", showDate)
                        .param("to", showDate))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalBookings").value(1))
                .andExpect(jsonPath("$.totalRevenue").value(selectedShow.path("ticketPrice").decimalValue().doubleValue()))
                .andExpect(jsonPath("$.bookings.length()").value(1))
                .andExpect(jsonPath("$.bookings[0].customerEmail").value("admin@test.local"));

        mockMvc.perform(get("/api/admin/dashboard")
                        .session(adminSession)
                        .param("from", "2020-01-01")
                        .param("to", "2019-12-31"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void customerRegistrationDoesNotGrantAdminRole() throws Exception {
        MockHttpSession session = new MockHttpSession();
        String actualToken = mockMvc.perform(get("/api/auth/csrf").session(session))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String token = objectMapper.readTree(actualToken).path("token").asText();
        mockMvc.perform(post("/api/auth/register")
                        .session(session)
                        .header("X-CSRF-Token", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"displayName":"New Customer","email":"new-customer@test.local","password":"customer-password"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }

    private static MockHttpSession sessionFor(CustomerAccount account) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(CustomerSession.ACCOUNT_ID_ATTRIBUTE, account.getId());
        return session;
    }

    private String csrfToken(MockHttpSession session) throws Exception {
        String response = mockMvc.perform(get("/api/auth/csrf").session(session))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).path("token").asText();
    }
}
