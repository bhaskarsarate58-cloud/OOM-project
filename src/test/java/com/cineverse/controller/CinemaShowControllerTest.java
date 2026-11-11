package com.cineverse.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.cineverse.config.DevelopmentMovieDataLoader;
import com.cineverse.dto.CinemaRequest;
import com.cineverse.dto.ShowRequest;
import com.cineverse.entity.Cinema;
import com.cineverse.entity.Movie;
import com.cineverse.entity.Screen;
import com.cineverse.entity.Show;
import com.cineverse.entity.ShowStatus;
import com.cineverse.exception.RequestConflictException;
import com.cineverse.repository.CinemaRepository;
import com.cineverse.repository.MovieRepository;
import com.cineverse.repository.ScreenRepository;
import com.cineverse.repository.ShowRepository;
import com.cineverse.service.ShowService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CinemaShowControllerTest {

    private static final String TEST_MOVIE_TITLE = "Phase 3 Test Movie";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private CinemaRepository cinemaRepository;

    @Autowired
    private ScreenRepository screenRepository;

    @Autowired
    private ShowRepository showRepository;

    @Autowired
    private DevelopmentMovieDataLoader dataLoader;

    @Autowired
    private ShowService showService;

    private Long movieId;
    private Long cinemaId;
    private Long screenId;

    @BeforeEach
    void setUpTestCinemaAndScreen() {
        showRepository.deleteAll();
        screenRepository.deleteAll();
        cinemaRepository.deleteAll();

        Movie movie = movieRepository.findFirstByTitleOrderByIdAsc(TEST_MOVIE_TITLE)
                .orElseGet(() -> movieRepository.save(new Movie(
                        TEST_MOVIE_TITLE,
                        "A movie used by the Phase 3 integration tests.",
                        "Drama",
                        "English",
                        120,
                        new BigDecimal("8.0"),
                        null,
                        LocalDate.of(2025, 1, 1))));
        movieId = movie.getId();

        Cinema cinema = cinemaRepository.save(new Cinema(
                "Phase 3 Test Cinema",
                "1 Test Street",
                "Bengaluru",
                List.of("Parking", "Snack bar")));
        cinemaId = cinema.getId();

        Screen screen = screenRepository.save(new Screen("Screen A", 8, 12, cinema));
        screenId = screen.getId();
    }

    @Test
    void cinemaListAndDetailsIncludeItsScreens() throws Exception {
        mockMvc.perform(get("/api/cinemas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Phase 3 Test Cinema"))
                .andExpect(jsonPath("$[0].facilities[0]").value("Parking"))
                .andExpect(jsonPath("$[0].screens[0].name").value("Screen A"))
                .andExpect(jsonPath("$[0].screens[0].rowCount").value(8))
                .andExpect(jsonPath("$[0].screens[0].seatsPerRow").value(12))
                .andExpect(jsonPath("$[0].screens[0].totalSeatCapacity").value(96));

        mockMvc.perform(get("/api/cinemas/{id}", cinemaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(cinemaId))
                .andExpect(jsonPath("$.screens[0].totalSeatCapacity").value(96));
    }

    @Test
    void cinemaCanBeCreatedAndUpdated() throws Exception {
        CinemaRequest createRequest = new CinemaRequest(
                "New Cinema",
                "42 New Street",
                "Chennai",
                List.of("IMAX", "Parking"));

        mockMvc.perform(post("/api/cinemas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.matchesPattern("/api/cinemas/\\d+")))
                .andExpect(jsonPath("$.name").value("New Cinema"))
                .andExpect(jsonPath("$.screens").isEmpty());

        CinemaRequest updateRequest = new CinemaRequest(
                "Updated Cinema",
                "99 Updated Street",
                "Hyderabad",
                List.of("Dolby Atmos"));

        mockMvc.perform(put("/api/cinemas/{id}", cinemaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Cinema"))
                .andExpect(jsonPath("$.city").value("Hyderabad"))
                .andExpect(jsonPath("$.facilities[0]").value("Dolby Atmos"))
                .andExpect(jsonPath("$.screens[0].name").value("Screen A"));
    }

    @Test
    void deletingCinemaAlsoRemovesOwnedScreensAndShows() throws Exception {
        Movie movie = movieRepository.findById(movieId).orElseThrow();
        Screen screen = screenRepository.findById(screenId).orElseThrow();
        Show savedShow = showRepository.save(new Show(
                movie,
                screen,
                futureStart(20, 18, 0),
                futureStart(20, 20, 0),
                new BigDecimal("350.00"),
                ShowStatus.SCHEDULED));

        mockMvc.perform(delete("/api/cinemas/{id}", cinemaId))
                .andExpect(status().isNoContent());

        assertFalse(cinemaRepository.existsById(cinemaId));
        assertFalse(screenRepository.existsById(screenId));
        assertFalse(showRepository.existsById(savedShow.getId()));
    }

    @Test
    void invalidCinemaRequestReturnsFieldErrors() throws Exception {
        CinemaRequest invalidRequest = new CinemaRequest(" ", "", "", List.of());

        mockMvc.perform(post("/api/cinemas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.name").exists())
                .andExpect(jsonPath("$.fieldErrors.address").exists())
                .andExpect(jsonPath("$.fieldErrors.city").exists())
                .andExpect(jsonPath("$.fieldErrors.facilities").exists());
    }

    @Test
    void missingCinemaReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/cinemas/{id}", 999999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Cinema with id 999999 was not found"));
    }

    @Test
    void showsCanBeListedReadAndFilteredByMovieOrCinema() throws Exception {
        Show savedShow = showRepository.save(createShow(
                futureStart(20, 18, 0),
                futureStart(20, 20, 0),
                screenId));

        mockMvc.perform(get("/api/shows"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].movieTitle").value(TEST_MOVIE_TITLE))
                .andExpect(jsonPath("$[0].cinemaName").value("Phase 3 Test Cinema"))
                .andExpect(jsonPath("$[0].screenName").value("Screen A"));

        mockMvc.perform(get("/api/shows/{id}", savedShow.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedShow.getId()));

        mockMvc.perform(get("/api/movies/{movieId}/shows", movieId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(get("/api/cinemas/{cinemaId}/shows", cinemaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void showCanBeCreated() throws Exception {
        ShowRequest request = showRequest(
                movieId,
                screenId,
                futureStart(22, 18, 0),
                futureStart(22, 20, 30),
                new BigDecimal("425.00"));

        mockMvc.perform(post("/api/shows")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.matchesPattern("/api/shows/\\d+")))
                .andExpect(jsonPath("$.movieTitle").value(TEST_MOVIE_TITLE))
                .andExpect(jsonPath("$.cinemaId").value(cinemaId))
                .andExpect(jsonPath("$.screenId").value(screenId))
                .andExpect(jsonPath("$.totalSeatCapacity").value(96))
                .andExpect(jsonPath("$.ticketPrice").value(425.0))
                .andExpect(jsonPath("$.status").value("SCHEDULED"));
    }

    @Test
    void overlappingScheduledShowsOnTheSameScreenReturnConflict() throws Exception {
        showRepository.save(createShow(
                futureStart(25, 18, 0),
                futureStart(25, 20, 0),
                screenId));

        ShowRequest overlap = showRequest(
                movieId,
                screenId,
                futureStart(25, 19, 30),
                futureStart(25, 21, 0),
                new BigDecimal("300.00"));

        mockMvc.perform(post("/api/shows")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(overlap)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("This screen already has a scheduled show during the requested time"));
    }

    @Test
    void concurrentOverlappingRequestsAllowOnlyOneScheduledShow() throws Exception {
        LocalDateTime start = futureStart(25, 18, 0);
        ShowRequest firstRequest = showRequest(
                movieId,
                screenId,
                start,
                start.plusHours(2),
                new BigDecimal("350.00"));
        ShowRequest secondRequest = showRequest(
                movieId,
                screenId,
                start.plusMinutes(30),
                start.plusHours(2).plusMinutes(30),
                new BigDecimal("350.00"));

        CountDownLatch startTogether = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Future<Boolean> first = executor.submit(() -> trySchedule(startTogether, firstRequest));
            Future<Boolean> second = executor.submit(() -> trySchedule(startTogether, secondRequest));
            startTogether.countDown();

            int acceptedRequests = (first.get(10, TimeUnit.SECONDS) ? 1 : 0)
                    + (second.get(10, TimeUnit.SECONDS) ? 1 : 0);

            assertEquals(1, acceptedRequests);
            assertEquals(1L, showRepository.count());
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void adjacentShowsAndShowsOnDifferentScreensAreAllowed() throws Exception {
        LocalDateTime firstStart = futureStart(26, 18, 0);
        LocalDateTime firstEnd = futureStart(26, 20, 0);
        showRepository.save(createShow(firstStart, firstEnd, screenId));

        ShowRequest adjacent = showRequest(
                movieId,
                screenId,
                firstEnd,
                futureStart(26, 22, 0),
                new BigDecimal("300.00"));
        mockMvc.perform(post("/api/shows")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adjacent)))
                .andExpect(status().isCreated());

        Screen secondScreen = screenRepository.save(new Screen(
                "Screen B",
                6,
                10,
                cinemaRepository.findById(cinemaId).orElseThrow()));
        ShowRequest sameTimeDifferentScreen = showRequest(
                movieId,
                secondScreen.getId(),
                firstStart,
                firstEnd,
                new BigDecimal("300.00"));
        mockMvc.perform(post("/api/shows")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sameTimeDifferentScreen)))
                .andExpect(status().isCreated());
    }

    @Test
    void invalidShowRequestReturnsValidationErrors() throws Exception {
        LocalDateTime past = LocalDateTime.now().minusDays(1);
        ShowRequest invalidRequest = showRequest(
                movieId,
                screenId,
                past,
                past.minusHours(1),
                new BigDecimal("-1.00"));

        mockMvc.perform(post("/api/shows")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.startTime").exists())
                .andExpect(jsonPath("$.fieldErrors.endTimeAfterStartTime").exists())
                .andExpect(jsonPath("$.fieldErrors.ticketPrice").exists());
    }

    @Test
    void missingMovieScreenCinemaAndShowReturnUsefulNotFoundErrors() throws Exception {
        ShowRequest missingMovie = showRequest(
                999999L,
                screenId,
                futureStart(28, 18, 0),
                futureStart(28, 20, 0),
                new BigDecimal("300.00"));
        mockMvc.perform(post("/api/shows")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(missingMovie)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Movie with id 999999 was not found"));

        ShowRequest missingScreen = showRequest(
                movieId,
                999999L,
                futureStart(28, 18, 0),
                futureStart(28, 20, 0),
                new BigDecimal("300.00"));
        mockMvc.perform(post("/api/shows")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(missingScreen)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Screen with id 999999 was not found"));

        mockMvc.perform(get("/api/cinemas/{id}/shows", 999999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Cinema with id 999999 was not found"));

        mockMvc.perform(get("/api/movies/{id}/shows", 999999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Movie with id 999999 was not found"));

        mockMvc.perform(get("/api/shows/{id}", 999999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Show with id 999999 was not found"));
    }

    @Test
    void developmentDataLoaderIsIdempotent() {
        showRepository.deleteAll();
        screenRepository.deleteAll();
        cinemaRepository.deleteAll();
        movieRepository.deleteAll();

        dataLoader.run(new DefaultApplicationArguments(new String[0]));
        long movieCount = movieRepository.count();
        long cinemaCount = cinemaRepository.count();
        long screenCount = screenRepository.count();
        long showCount = showRepository.count();

        dataLoader.run(new DefaultApplicationArguments(new String[0]));

        assertEquals(3L, movieCount);
        assertEquals(2L, cinemaCount);
        assertEquals(3L, screenCount);
        assertEquals(3L, showCount);
        assertEquals(movieCount, movieRepository.count());
        assertEquals(cinemaCount, cinemaRepository.count());
        assertEquals(screenCount, screenRepository.count());
        assertEquals(showCount, showRepository.count());
    }

    private Show createShow(LocalDateTime start, LocalDateTime end, Long selectedScreenId) {
        Movie movie = movieRepository.findById(movieId).orElseThrow();
        Screen screen = screenRepository.findById(selectedScreenId).orElseThrow();
        return new Show(
                movie,
                screen,
                start,
                end,
                new BigDecimal("350.00"),
                ShowStatus.SCHEDULED);
    }

    private ShowRequest showRequest(
            Long selectedMovieId,
            Long selectedScreenId,
            LocalDateTime start,
            LocalDateTime end,
            BigDecimal ticketPrice) {
        return new ShowRequest(
                selectedMovieId,
                selectedScreenId,
                start,
                end,
                ticketPrice,
                ShowStatus.SCHEDULED);
    }

    private LocalDateTime futureStart(int daysFromToday, int hour, int minute) {
        return LocalDate.now().plusDays(daysFromToday).atTime(hour, minute);
    }

    private boolean trySchedule(CountDownLatch startTogether, ShowRequest request) throws InterruptedException {
        startTogether.await();
        try {
            showService.createShow(request);
            return true;
        } catch (RequestConflictException conflict) {
            return false;
        }
    }
}
