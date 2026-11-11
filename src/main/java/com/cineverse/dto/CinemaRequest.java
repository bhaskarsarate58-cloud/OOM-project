package com.cineverse.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record CinemaRequest(
        @NotBlank(message = "Cinema name is required")
        @Size(max = 120, message = "Cinema name must be at most 120 characters")
        String name,

        @NotBlank(message = "Cinema address is required")
        @Size(max = 250, message = "Cinema address must be at most 250 characters")
        String address,

        @NotBlank(message = "City is required")
        @Size(max = 100, message = "City must be at most 100 characters")
        String city,

        @NotEmpty(message = "At least one facility is required")
        @Size(max = 20, message = "A cinema can list at most 20 facilities")
        List<@NotBlank(message = "Facility names cannot be blank")
             @Size(max = 80, message = "Facility names must be at most 80 characters") String> facilities) {
}
