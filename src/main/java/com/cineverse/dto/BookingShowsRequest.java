package com.cineverse.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record BookingShowsRequest(
        @NotBlank @Pattern(regexp = "local|tmdb|curated") String source,
        @NotBlank @Size(max = 20) String movieId,
        @NotBlank @Size(max = 100) String city,
        @NotNull LocalDate date) {
}
