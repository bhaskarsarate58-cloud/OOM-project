package com.cineverse.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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

import com.cineverse.entity.Cinema;
import com.cineverse.entity.CustomerAccount;
import com.cineverse.entity.Movie;
import com.cineverse.entity.Screen;
import com.cineverse.entity.Show;
import com.cineverse.entity.ShowStatus;
import com.cineverse.repository.BookingRepository;
import com.cineverse.repository.CinemaRepository;
import com.cineverse.repository.CustomerAccountRepository;
import com.cineverse.repository.MovieRepository;
import com.cineverse.repository.ScreenRepository;
import com.cineverse.repository.ShowRepository;
import com.cineverse.service.CustomerSession;

@SpringBootTest
@AutoConfigureMockMvc
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ShowRepository showRepository;

    @Autowired
    private ScreenRepository screenRepository;

    @Autowired
    private CinemaRepository cinemaRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private CustomerAccountRepository customerAccountRepository;

    private Long showId;
    private MockHttpSession customerSession;
    private MockHttpSession otherCustomerSession;

    @BeforeEach
    void createFutureShow() {
        bookingRepository.deleteAll();
        showRepository.deleteAll();
        screenRepository.deleteAll();
        cinemaRepository.deleteAll();

        Movie movie = movieRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("Development movie data was not seeded"));
        Cinema cinema = cinemaRepository.save(new Cinema(
                "Booking Test Cinema",
                "5 Test Road",
                "Bengaluru",
                List.of("Parking")));
        Screen screen = screenRepository.save(new Screen("Booking Screen", 3, 4, cinema));
        LocalDateTime startTime = LocalDateTime.now().plusDays(3);
        Show show = showRepository.save(new Show(
                movie,
                screen,
                startTime,
                startTime.plusHours(2),
                new BigDecimal("12.50"),
                ShowStatus.SCHEDULED));
        showId = show.getId();
        customerSession = createCustomerSession();
        otherCustomerSession = createCustomerSession();
    }

    @Test
    void createsConfirmedBookingWithCalculatedTotalAndRetrievesIt() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/bookings")
                        .session(customerSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingRequest(List.of("A1", "A2"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bookingReference").isNotEmpty())
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.showId").value(showId))
                .andExpect(jsonPath("$.seatLabels[0]").value("A1"))
                .andExpect(jsonPath("$.seatLabels[1]").value("A2"))
                .andExpect(jsonPath("$.seatSubtotal").value(31.26))
                .andExpect(jsonPath("$.foodSubtotal").value(0.0))
                .andExpect(jsonPath("$.totalPrice").value(31.26))
                .andReturn();

        JsonNode response = objectMapper.readTree(createResult.getResponse().getContentAsString());
        long bookingId = response.get("id").asLong();

        mockMvc.perform(get("/api/shows/{showId}/seats", showId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(12))
                .andExpect(jsonPath("$[0].label").value("A1"))
                .andExpect(jsonPath("$[0].available").value(false))
                .andExpect(jsonPath("$[2].label").value("A3"))
                .andExpect(jsonPath("$[2].available").value(true));

        mockMvc.perform(get("/api/bookings/{bookingId}", bookingId).session(customerSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingId))
                .andExpect(jsonPath("$.bookingReference").value(response.get("bookingReference").asText()))
                .andExpect(jsonPath("$.seatLabels.length()").value(2))
                .andExpect(jsonPath("$.totalPrice").value(31.26));
    }

    @Test
    void pricesPremiumAndStandardSeatsAndAddsFoodToTheSavedBooking() throws Exception {
        mockMvc.perform(get("/api/booking-food-menu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0].itemId").value("popcorn"))
                .andExpect(jsonPath("$[0].unitPrice").value(120.0));

        String request = objectMapper.writeValueAsString(new com.cineverse.dto.CreateBookingRequest(
                showId,
                List.of("A1", "C1"),
                "upi",
                "SUCCESS",
                List.of(new com.cineverse.dto.FoodOrderRequest("popcorn", 1))));

        MvcResult result = mockMvc.perform(post("/api/bookings")
                        .session(customerSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.seatSubtotal").value(28.13))
                .andExpect(jsonPath("$.foodSubtotal").value(120.0))
                .andExpect(jsonPath("$.totalPrice").value(148.13))
                .andExpect(jsonPath("$.foodItems[0].itemId").value("popcorn"))
                .andExpect(jsonPath("$.foodItems[0].quantity").value(1))
                .andReturn();

        long bookingId = objectMapper.readTree(result.getResponse().getContentAsString()).path("id").asLong();
        mockMvc.perform(get("/api/bookings/{bookingId}", bookingId).session(customerSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.foodItems[0].name").value("Classic popcorn"))
                .andExpect(jsonPath("$.foodItems[0].totalPrice").value(120.0));
    }

    @Test
    void usesConfiguredShowsBeforeCreatingClearlyLabelledDemoShowtimes() throws Exception {
        Movie movie = showRepository.findById(showId).orElseThrow().getMovie();
        LocalDate showDate = showRepository.findById(showId).orElseThrow().getStartTime().toLocalDate();
        String configuredRequest = objectMapper.writeValueAsString(new com.cineverse.dto.BookingShowsRequest(
                "local", movie.getId().toString(), "Bengaluru", showDate));

        mockMvc.perform(post("/api/movies/booking-shows")
                        .session(customerSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(configuredRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].demo").value(false))
                .andExpect(jsonPath("$[0].city").value("Bengaluru"))
                .andExpect(jsonPath("$[0].id").value(showId))
                .andExpect(jsonPath("$[1].cinemaName").value("CineVerse Demo Cinema"))
                .andExpect(jsonPath("$[2].cinemaName").value("CineVerse Demo Cinema 2"));

        String demoRequest = objectMapper.writeValueAsString(new com.cineverse.dto.BookingShowsRequest(
                "local", movie.getId().toString(), "Prayagraj", showDate));
        MvcResult demoResult = mockMvc.perform(post("/api/movies/booking-shows")
                        .session(customerSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(demoRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].demo").value(true))
                .andExpect(jsonPath("$[0].cinemaName").value("CineVerse Demo Cinema"))
                .andExpect(jsonPath("$[0].city").value("Prayagraj"))
                .andReturn();

        long demoShowId = objectMapper.readTree(demoResult.getResponse().getContentAsString())
                .get(0).get("id").asLong();
        mockMvc.perform(post("/api/movies/booking-shows")
                        .session(customerSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(demoRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].id").value(demoShowId));
        String secondDemoDateRequest = objectMapper.writeValueAsString(new com.cineverse.dto.BookingShowsRequest(
                "local", movie.getId().toString(), "Prayagraj", showDate.plusDays(1)));
        mockMvc.perform(post("/api/movies/booking-shows")
                        .session(customerSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(secondDemoDateRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].demo").value(true));
        String dateWithoutConfiguredShows = objectMapper.writeValueAsString(new com.cineverse.dto.BookingShowsRequest(
                "local", movie.getId().toString(), "Bengaluru", showDate.plusDays(1)));
        mockMvc.perform(post("/api/movies/booking-shows")
                        .session(customerSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dateWithoutConfiguredShows))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].demo").value(true));
        String bookingRequest = objectMapper.writeValueAsString(
                new com.cineverse.dto.CreateBookingRequest(demoShowId, List.of("A1", "A2")));
        MvcResult bookingResult = mockMvc.perform(post("/api/bookings")
                        .session(customerSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalPrice").value(497.50))
                .andExpect(jsonPath("$.city").value("Prayagraj"))
                .andReturn();
        long bookingId = objectMapper.readTree(bookingResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/api/auth/me/bookings").session(customerSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(bookingId))
                .andExpect(jsonPath("$[0].city").value("Prayagraj"));
    }

    @Test
    void requiresCustomerLoginBeforeGeneratingBookingShowtimes() throws Exception {
        Movie movie = showRepository.findById(showId).orElseThrow().getMovie();
        String request = objectMapper.writeValueAsString(new com.cineverse.dto.BookingShowsRequest(
                "local", movie.getId().toString(), "Delhi", LocalDate.now().plusDays(2)));

        mockMvc.perform(post("/api/movies/booking-shows")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void curatedFallbackMovieCanUseDemoBookingWithoutTmdbAccess() throws Exception {
        String request = objectMapper.writeValueAsString(new com.cineverse.dto.BookingShowsRequest(
                "curated", "inception", "Mumbai", LocalDate.now().plusDays(2)));

        mockMvc.perform(post("/api/discovery/movies/booking-shows")
                        .session(customerSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].movieTitle").value("Inception"))
                .andExpect(jsonPath("$[0].demo").value(true))
                .andExpect(jsonPath("$[0].city").value("Mumbai"));
    }

    @Test
    void rejectsSeatsAlreadyBookedForTheSameShow() throws Exception {
        mockMvc.perform(post("/api/bookings")
                        .session(customerSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingRequest(List.of("A1"))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/bookings")
                        .session(customerSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingRequest(List.of("A1"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("A1")));
    }

    @Test
    void storesDemoPaymentMethodOnTheConfirmedTicket() throws Exception {
        String request = objectMapper.writeValueAsString(
                new com.cineverse.dto.CreateBookingRequest(showId, List.of("A1"), "upi"));

        mockMvc.perform(post("/api/bookings")
                        .session(customerSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paymentMethod").value("UPI"))
                .andExpect(jsonPath("$.paymentStatus").value("SIMULATED_SUCCESS"))
                .andExpect(jsonPath("$.cinemaAddress").value("5 Test Road"));
    }

    @Test
    void rejectsEmptyDuplicateAndUnknownSeatSelectionsWithUsefulErrors() throws Exception {
        mockMvc.perform(post("/api/bookings")
                        .session(customerSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingRequest(List.of())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.seatLabels").exists());

        mockMvc.perform(post("/api/bookings")
                        .session(customerSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingRequest(List.of("A1", "A1"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.seatSelectionUnique").value("Choose each seat only once"));

        mockMvc.perform(post("/api/bookings")
                        .session(customerSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingRequest(List.of("Z99"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.seatLabels").value("Unknown seat label(s): Z99"));
    }

    @Test
    void rejectsConcurrentAttemptsToBookTheSameSeat() throws Exception {
        String requestBody = bookingRequest(List.of("B2"));
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<Integer> first = executor.submit(() -> postBookingAtSameTime(requestBody, customerSession, ready, start));
            Future<Integer> second = executor.submit(() -> postBookingAtSameTime(requestBody, customerSession, ready, start));
            assertTrue(ready.await(5, java.util.concurrent.TimeUnit.SECONDS));
            start.countDown();

            List<Integer> results = List.of(
                    first.get(10, java.util.concurrent.TimeUnit.SECONDS),
                    second.get(10, java.util.concurrent.TimeUnit.SECONDS));
            assertTrue(results.contains(201), "One request should reserve the seat");
            assertTrue(results.contains(409), "The competing request should receive a conflict");
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void requiresLoginBeforeCreatingBooking() throws Exception {
        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingRequest(List.of("A1"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Please log in to continue with your booking"));
    }

    @Test
    void cancellationRequiresOwnerIsSingleUseAndReleasesSeats() throws Exception {
        MvcResult created = mockMvc.perform(post("/api/bookings")
                        .session(customerSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingRequest(List.of("A1"))))
                .andExpect(status().isCreated())
                .andReturn();
        long bookingId = objectMapper.readTree(created.getResponse().getContentAsString()).path("id").asLong();

        mockMvc.perform(post("/api/bookings/{bookingId}/cancel", bookingId)
                        .session(otherCustomerSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"Changed plans\"}"))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/bookings/{bookingId}/cancel", bookingId)
                        .session(customerSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"Changed plans\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.cancellationReason").value("Changed plans"));

        mockMvc.perform(get("/api/shows/{showId}/seats", showId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].label").value("A1"))
                .andExpect(jsonPath("$[0].available").value(true));

        mockMvc.perform(post("/api/bookings/{bookingId}/cancel", bookingId)
                        .session(customerSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"Duplicate attempt\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void onlyOwnerCanRemoveCancelledBookings() throws Exception {
        MvcResult created = mockMvc.perform(post("/api/bookings")
                        .session(customerSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingRequest(List.of("A1"))))
                .andExpect(status().isCreated())
                .andReturn();
        long bookingId = objectMapper.readTree(created.getResponse().getContentAsString()).path("id").asLong();

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/api/bookings/{bookingId}", bookingId)
                        .session(customerSession))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/api/bookings/{bookingId}/cancel", bookingId)
                        .session(customerSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"Changed plans\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/api/bookings/{bookingId}", bookingId)
                        .session(otherCustomerSession))
                .andExpect(status().isNotFound());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/api/bookings/{bookingId}", bookingId)
                        .session(customerSession))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/bookings/{bookingId}", bookingId).session(customerSession))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/shows/{showId}/seats", showId))
                .andExpect(jsonPath("$[0].available").value(true));
    }

    @Test
    void recordsOneOwnerOnlyDemoRefundOrExchangeRequest() throws Exception {
        MvcResult created = mockMvc.perform(post("/api/bookings")
                        .session(customerSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingRequest(List.of("A2"))))
                .andExpect(status().isCreated())
                .andReturn();
        long bookingId = objectMapper.readTree(created.getResponse().getContentAsString()).path("id").asLong();
        mockMvc.perform(post("/api/bookings/{bookingId}/cancel", bookingId)
                        .session(customerSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"Schedule changed\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/bookings/{bookingId}/aftercare", bookingId)
                        .session(otherCustomerSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"REFUND\"}"))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/bookings/{bookingId}/aftercare", bookingId)
                        .session(customerSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"REFUND\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aftercareType").value("REFUND"))
                .andExpect(jsonPath("$.aftercareStatus").value("PENDING"));

        mockMvc.perform(post("/api/bookings/{bookingId}/aftercare", bookingId)
                        .session(customerSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"EXCHANGE\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void rejectsMalformedBookingRequests() throws Exception {
        mockMvc.perform(post("/api/bookings")
                        .session(customerSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"showId\":0,\"seatLabels\":[\"A!\"],\"paymentMethod\":\"upi\",\"paymentOutcome\":\"SUCCESS\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.showId").exists())
                .andExpect(jsonPath("$.fieldErrors['seatLabels[0]']").exists());
    }

    @Test
    void rejectsFailedDemoPaymentWithoutCreatingBooking() throws Exception {
        String request = objectMapper.writeValueAsString(
                new com.cineverse.dto.CreateBookingRequest(showId, List.of("A1"), "card", "FAILURE"));
        mockMvc.perform(post("/api/bookings")
                        .session(customerSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
        org.junit.jupiter.api.Assertions.assertEquals(0, bookingRepository.count());
    }

    private Integer postBookingAtSameTime(
            String requestBody,
            MockHttpSession session,
            CountDownLatch ready,
            CountDownLatch start) throws Exception {
        ready.countDown();
        if (!start.await(5, java.util.concurrent.TimeUnit.SECONDS)) {
            throw new IllegalStateException("Concurrent booking test did not start");
        }
        return mockMvc.perform(post("/api/bookings")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andReturn()
                .getResponse()
                .getStatus();
    }

    private String bookingRequest(List<String> seatLabels) throws Exception {
        return objectMapper.writeValueAsString(
                new com.cineverse.dto.CreateBookingRequest(showId, seatLabels, "upi", "SUCCESS"));
    }

    private MockHttpSession createCustomerSession() {
        String email = "booking-" + UUID.randomUUID() + "@example.com";
        CustomerAccount customer = customerAccountRepository.save(
                new CustomerAccount(email, "Booking Customer", "test-password-hash"));
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(CustomerSession.ACCOUNT_ID_ATTRIBUTE, customer.getId());
        return session;
    }
}
