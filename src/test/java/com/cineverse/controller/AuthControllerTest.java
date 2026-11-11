package com.cineverse.controller;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.cineverse.repository.CustomerAccountRepository;
import com.cineverse.repository.BookingRepository;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomerAccountRepository accountRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @BeforeEach
    void clearAccounts() {
        bookingRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @Test
    void registersHashedAccountAndReturnsAuthenticatedProfile() throws Exception {
        CsrfSession csrf = csrfSession();
        String originalSessionId = csrf.session().getId();

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .session(csrf.session())
                        .header("X-CSRF-Token", csrf.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"displayName":"Cine Viewer","email":"Viewer@Example.com","password":"movie-night-2026"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.displayName").value("Cine Viewer"))
                .andExpect(jsonPath("$.email").value("viewer@example.com"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andReturn();

        assertNotEquals(originalSessionId, result.getRequest().getSession(false).getId());
        var account = accountRepository.findByEmail("viewer@example.com").orElseThrow();
        assertNotEquals("movie-night-2026", account.getPasswordHash());
        assertNotNull(account.getPasswordHash());
        mockMvc.perform(get("/api/auth/me").session(csrf.session()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("viewer@example.com"));
    }

    @Test
    void loginRejectsWrongPasswordAndLogoutEndsSession() throws Exception {
        CsrfSession csrf = csrfSession();
        mockMvc.perform(post("/api/auth/register")
                        .session(csrf.session())
                        .header("X-CSRF-Token", csrf.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody("viewer@example.com", "movie-night-2026")))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/login")
                        .session(csrf.session())
                        .header("X-CSRF-Token", csrf.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"viewer@example.com","password":"incorrect-password"}
                                """))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/auth/login")
                        .session(csrf.session())
                        .header("X-CSRF-Token", csrf.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"VIEWER@example.com","password":"movie-night-2026"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("viewer@example.com"));

        mockMvc.perform(post("/api/auth/logout")
                        .session(csrf.session())
                        .header("X-CSRF-Token", csrf.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loggedOut").value(true));
        mockMvc.perform(get("/api/auth/me").session(csrf.session()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void requiresCsrfAndValidRegistrationFields() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody("viewer@example.com", "movie-night-2026")))
                .andExpect(status().isForbidden());

        CsrfSession csrf = csrfSession();
        mockMvc.perform(post("/api/auth/register")
                        .session(csrf.session())
                        .header("X-CSRF-Token", csrf.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody("not-an-email", "short")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.email").exists())
                .andExpect(jsonPath("$.fieldErrors.password").exists());
    }

    @Test
    void customerBookingReportAndBookingLookupAreRestrictedToOwner() throws Exception {
        CsrfSession ownerCsrf = csrfSession();
        mockMvc.perform(post("/api/auth/register")
                        .session(ownerCsrf.session())
                        .header("X-CSRF-Token", ownerCsrf.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody("owner@example.com", "movie-night-2026")))
                .andExpect(status().isCreated());

        JsonNode shows = objectMapper.readTree(mockMvc.perform(get("/api/shows"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
        long showId = shows.get(0).get("id").asLong();
        JsonNode seats = objectMapper.readTree(mockMvc.perform(get("/api/shows/{showId}/seats", showId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
        String availableSeat = null;
        for (JsonNode seat : seats) {
            if (seat.path("available").asBoolean()) {
                availableSeat = seat.path("label").asText();
                break;
            }
        }
        assertNotNull(availableSeat);

        MvcResult bookingResult = mockMvc.perform(post("/api/bookings")
                        .session(ownerCsrf.session())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"showId\":" + showId + ",\"seatLabels\":[\"" + availableSeat + "\"]}"))
                .andExpect(status().isCreated())
                .andReturn();
        long bookingId = objectMapper.readTree(bookingResult.getResponse().getContentAsString()).path("id").asLong();
        mockMvc.perform(get("/api/auth/me/bookings").session(ownerCsrf.session()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(bookingId));

        CsrfSession otherCsrf = csrfSession();
        mockMvc.perform(post("/api/auth/register")
                        .session(otherCsrf.session())
                        .header("X-CSRF-Token", otherCsrf.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody("other@example.com", "movie-night-2026")))
                .andExpect(status().isCreated());
        mockMvc.perform(get("/api/auth/me/bookings").session(otherCsrf.session()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        mockMvc.perform(get("/api/bookings/{bookingId}", bookingId).session(otherCsrf.session()))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/bookings/{bookingId}", bookingId).session(ownerCsrf.session()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingId));
    }

    private CsrfSession csrfSession() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/auth/csrf"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return new CsrfSession((MockHttpSession) result.getRequest().getSession(false), json.get("token").asText());
    }

    private String registerBody(String email, String password) {
        return "{\"displayName\":\"Cine Viewer\",\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
    }

    private record CsrfSession(MockHttpSession session, String token) {
    }
}
