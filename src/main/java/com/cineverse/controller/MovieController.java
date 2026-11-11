package com.cineverse.controller;

import java.net.URI;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cineverse.dto.MovieDto;
import com.cineverse.dto.MovieRequest;
import com.cineverse.dto.BookingShowsRequest;
import com.cineverse.dto.ShowDto;
import com.cineverse.exception.AuthenticationRequiredException;
import com.cineverse.service.BookingShowsService;
import com.cineverse.service.CustomerSession;
import com.cineverse.service.MovieService;

@Validated
@RestController
@RequestMapping("/api/movies")
public class MovieController {

    private final MovieService movieService;
    private final BookingShowsService bookingShowsService;

    public MovieController(MovieService movieService, BookingShowsService bookingShowsService) {
        this.movieService = movieService;
        this.bookingShowsService = bookingShowsService;
    }

    @GetMapping
    public List<MovieDto> getAllMovies() {
        return movieService.getAllMovies();
    }

    @GetMapping("/search")
    public List<MovieDto> searchMovies(
            @RequestParam("title")
            @NotBlank(message = "Search title is required")
            @Size(max = 150, message = "Search title must be at most 150 characters")
            String title) {
        return movieService.searchMoviesByTitle(title);
    }

    @GetMapping("/{id}")
    public MovieDto getMovieById(@PathVariable @Positive Long id) {
        return movieService.getMovieById(id);
    }

    @PostMapping("/booking-shows")
    public List<ShowDto> getBookingShows(
            @Valid @RequestBody BookingShowsRequest request,
            HttpServletRequest servletRequest) {
        if (CustomerSession.accountId(servletRequest.getSession(false)) == null) {
            throw new AuthenticationRequiredException();
        }
        if (!"local".equalsIgnoreCase(request.source())) {
            throw new com.cineverse.exception.InvalidRequestException(
                    "Choose a local movie for this endpoint",
                    java.util.Map.of("source", "Source must be local"));
        }
        return bookingShowsService.getBookingShows(
                request.source(), request.movieId(), request.city(), request.date());
    }

    @PostMapping
    public ResponseEntity<MovieDto> createMovie(@Valid @RequestBody MovieRequest request) {
        MovieDto movie = movieService.createMovie(request);
        return ResponseEntity.created(URI.create("/api/movies/" + movie.id())).body(movie);
    }

    @PutMapping("/{id}")
    public MovieDto updateMovie(
            @PathVariable @Positive Long id,
            @Valid @RequestBody MovieRequest request) {
        return movieService.updateMovie(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMovie(@PathVariable @Positive Long id) {
        movieService.deleteMovie(id);
        return ResponseEntity.noContent().build();
    }
}
