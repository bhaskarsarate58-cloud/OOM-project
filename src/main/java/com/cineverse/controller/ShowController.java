package com.cineverse.controller;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cineverse.dto.ShowDto;
import com.cineverse.dto.ShowRequest;
import com.cineverse.service.ShowService;

@Validated
@RestController
@RequestMapping("/api")
public class ShowController {

    private final ShowService showService;

    public ShowController(ShowService showService) {
        this.showService = showService;
    }

    @GetMapping("/shows")
    public List<ShowDto> getAllShows() {
        return showService.getAllShows();
    }

    @GetMapping("/shows/{id}")
    public ShowDto getShowById(@PathVariable @Positive Long id) {
        return showService.getShowById(id);
    }

    @PostMapping("/shows")
    public ResponseEntity<ShowDto> createShow(@Valid @RequestBody ShowRequest request) {
        ShowDto show = showService.createShow(request);
        return ResponseEntity.created(URI.create("/api/shows/" + show.id())).body(show);
    }

    @GetMapping("/movies/{movieId}/shows")
    public List<ShowDto> getShowsByMovie(@PathVariable @Positive Long movieId) {
        return showService.getShowsByMovie(movieId);
    }

    @GetMapping("/cinemas/{cinemaId}/shows")
    public List<ShowDto> getShowsByCinema(@PathVariable @Positive Long cinemaId) {
        return showService.getShowsByCinema(cinemaId);
    }
}
