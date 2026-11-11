package com.cineverse.controller;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.cineverse.dto.MovieRequest;
import com.cineverse.entity.Movie;
import com.cineverse.repository.MovieRepository;
import com.cineverse.repository.ShowRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureMockMvc
class MovieControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private ShowRepository showRepository;

    @BeforeEach
    void clearMovies() {
        showRepository.deleteAll();
        movieRepository.deleteAll();
    }

    @Test
    void getAllMoviesReturnsMoviesInTitleOrder() throws Exception {
        movieRepository.save(movie("Zootopia", "Animation", "English"));
        movieRepository.save(movie("Arrival", "Science Fiction", "English"));

        mockMvc.perform(get("/api/movies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Arrival"))
                .andExpect(jsonPath("$[1].title").value("Zootopia"));
    }

    @Test
    void getMovieByIdReturnsMovie() throws Exception {
        Movie savedMovie = movieRepository.save(movie("Arrival", "Science Fiction", "English"));

        mockMvc.perform(get("/api/movies/{id}", savedMovie.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedMovie.getId()))
                .andExpect(jsonPath("$.title").value("Arrival"));
    }

    @Test
    void createMovieReturnsCreatedMovieAndLocation() throws Exception {
        MovieRequest request = request("New Film", "Drama", "English");

        mockMvc.perform(post("/api/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.matchesPattern("/api/movies/\\d+")))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("New Film"))
                .andExpect(jsonPath("$.durationMinutes").value(120));

        assertEquals(1L, movieRepository.count());
    }

    @Test
    void updateMovieChangesExistingMovie() throws Exception {
        Movie savedMovie = movieRepository.save(movie("Old Title", "Drama", "English"));
        MovieRequest request = request("Updated Title", "Comedy", "Hindi");

        mockMvc.perform(put("/api/movies/{id}", savedMovie.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedMovie.getId()))
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.genre").value("Comedy"))
                .andExpect(jsonPath("$.language").value("Hindi"));

        mockMvc.perform(get("/api/movies/{id}", savedMovie.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    @Test
    void deleteMovieRemovesIt() throws Exception {
        Movie savedMovie = movieRepository.save(movie("To Delete", "Drama", "English"));

        mockMvc.perform(delete("/api/movies/{id}", savedMovie.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/movies/{id}", savedMovie.getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Movie with id " + savedMovie.getId() + " was not found"));
    }

    @Test
    void searchFindsTitlesWithoutCaseSensitivity() throws Exception {
        movieRepository.save(movie("The Matrix", "Science Fiction", "English"));
        movieRepository.save(movie("The Matrix Reloaded", "Science Fiction", "English"));
        movieRepository.save(movie("Arrival", "Science Fiction", "English"));

        mockMvc.perform(get("/api/movies/search").param("title", "mAtRiX"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("The Matrix"))
                .andExpect(jsonPath("$[1].title").value("The Matrix Reloaded"));
    }

    @Test
    void missingMovieReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/movies/{id}", 999999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path").value("/api/movies/999999"));
    }

    @Test
    void nonNumericMovieIdReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/movies/not-a-number"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.id").value("Value has the wrong format"));
    }

    @Test
    void invalidMovieRequestReturnsFieldErrors() throws Exception {
        MovieRequest invalidRequest = new MovieRequest(
                " ",
                "A description",
                "",
                "English",
                0,
                new BigDecimal("10.5"),
                "not-a-url",
                LocalDate.of(2020, 1, 1));

        mockMvc.perform(post("/api/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request validation failed"))
                .andExpect(jsonPath("$.fieldErrors.title").exists())
                .andExpect(jsonPath("$.fieldErrors.genre").exists())
                .andExpect(jsonPath("$.fieldErrors.durationMinutes").exists())
                .andExpect(jsonPath("$.fieldErrors.rating").exists())
                .andExpect(jsonPath("$.fieldErrors.posterUrl").exists());
    }

    @Test
    void blankSearchTitleReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/movies/search").param("title", " "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request validation failed"));
    }

    @Test
    void missingSearchTitleReturnsStructuredBadRequest() throws Exception {
        mockMvc.perform(get("/api/movies/search"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.title").value("This parameter is required"));
    }

    @Test
    void healthEndpointStillWorksWithTheFullApplication() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.application").value("CineVerse"));
    }

    private Movie movie(String title, String genre, String language) {
        return new Movie(
                title,
                "A sample movie description",
                genre,
                language,
                120,
                new BigDecimal("8.0"),
                null,
                LocalDate.of(2020, 1, 1));
    }

    private MovieRequest request(String title, String genre, String language) {
        return new MovieRequest(
                title,
                "A movie description",
                genre,
                language,
                120,
                new BigDecimal("8.2"),
                "https://example.com/poster.jpg",
                LocalDate.of(2024, 3, 1));
    }
}
