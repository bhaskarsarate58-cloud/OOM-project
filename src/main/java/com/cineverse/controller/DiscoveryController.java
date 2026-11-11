package com.cineverse.controller;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cineverse.dto.BookingShowsRequest;
import com.cineverse.dto.DiscoveryMovieDto;
import com.cineverse.dto.DiscoveryMoviesPageDto;
import com.cineverse.dto.DiscoveryStatusDto;
import com.cineverse.dto.ShowDto;
import com.cineverse.exception.AuthenticationRequiredException;
import com.cineverse.exception.InvalidRequestException;
import com.cineverse.service.BookingShowsService;
import com.cineverse.service.CustomerSession;
import com.cineverse.service.TmdbDiscoveryService;

@Validated
@RestController
@RequestMapping("/api/discovery")
public class DiscoveryController {

    private final TmdbDiscoveryService discoveryService;
    private final BookingShowsService bookingShowsService;

    public DiscoveryController(TmdbDiscoveryService discoveryService, BookingShowsService bookingShowsService) {
        this.discoveryService = discoveryService;
        this.bookingShowsService = bookingShowsService;
    }

    @GetMapping("/status")
    public DiscoveryStatusDto getStatus() {
        return discoveryService.getStatus();
    }

    @GetMapping("/movies")
    public DiscoveryMoviesPageDto getMovies(
            @RequestParam(defaultValue = "all")
            @Pattern(
                    regexp = "all|trending|popular|bollywood|hollywood|south-indian|korean|japanese|upcoming|top-rated|family-animation|action-adventure|horror-thriller|romance-drama|sci-fi-fantasy",
                    message = "Choose a supported movie category")
            String category,
            @RequestParam(defaultValue = "1") @Min(1) @Max(500) int page,
            @RequestParam(required = false) @Size(max = 150) String query) {
        return discoveryService.getMovies(category, page, query);
    }

    @GetMapping("/movies/{id}")
    public DiscoveryMovieDto getMovie(@PathVariable @Positive Long id) {
        return discoveryService.getMovie(id);
    }

    @PostMapping("/movies/booking-shows")
    public List<ShowDto> getBookingShows(
            @jakarta.validation.Valid @RequestBody BookingShowsRequest request,
            HttpServletRequest servletRequest) {
        if (CustomerSession.accountId(servletRequest.getSession(false)) == null) {
            throw new AuthenticationRequiredException();
        }
        if (!"tmdb".equalsIgnoreCase(request.source()) && !"curated".equalsIgnoreCase(request.source())) {
            throw new InvalidRequestException(
                    "Choose an online or curated movie",
                    java.util.Map.of("source", "Source must be tmdb or curated"));
        }
        return bookingShowsService.getBookingShows(
                request.source(), request.movieId(), request.city(), request.date());
    }
}
