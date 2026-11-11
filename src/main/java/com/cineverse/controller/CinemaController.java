package com.cineverse.controller;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cineverse.dto.CinemaDto;
import com.cineverse.dto.CinemaRequest;
import com.cineverse.service.CinemaService;

@Validated
@RestController
@RequestMapping("/api/cinemas")
public class CinemaController {

    private final CinemaService cinemaService;

    public CinemaController(CinemaService cinemaService) {
        this.cinemaService = cinemaService;
    }

    @GetMapping
    public List<CinemaDto> getAllCinemas() {
        return cinemaService.getAllCinemas();
    }

    @GetMapping("/{id}")
    public CinemaDto getCinemaById(@PathVariable @Positive Long id) {
        return cinemaService.getCinemaById(id);
    }

    @PostMapping
    public ResponseEntity<CinemaDto> createCinema(@Valid @RequestBody CinemaRequest request) {
        CinemaDto cinema = cinemaService.createCinema(request);
        return ResponseEntity.created(URI.create("/api/cinemas/" + cinema.id())).body(cinema);
    }

    @PutMapping("/{id}")
    public CinemaDto updateCinema(
            @PathVariable @Positive Long id,
            @Valid @RequestBody CinemaRequest request) {
        return cinemaService.updateCinema(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCinema(@PathVariable @Positive Long id) {
        cinemaService.deleteCinema(id);
        return ResponseEntity.noContent().build();
    }
}
