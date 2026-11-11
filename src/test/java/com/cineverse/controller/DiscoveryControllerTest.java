package com.cineverse.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "tmdb.api-key=")
@AutoConfigureMockMvc
class DiscoveryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void reportsLocalOnlyModeWhenTmdbKeyIsNotConfigured() throws Exception {
        mockMvc.perform(get("/api/discovery/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.liveAvailable").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("TMDB_API_KEY")));
    }

    @Test
    void explainsHowToEnableLiveMoviesWhenKeyIsMissing() throws Exception {
        mockMvc.perform(get("/api/discovery/movies").param("category", "trending"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("TMDB_API_KEY")));
    }

    @Test
    void rejectsUnsupportedMovieCategories() throws Exception {
        mockMvc.perform(get("/api/discovery/movies").param("category", "unsupported"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.category").exists());
    }
}
