package com.cineverse.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record MovieRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 150, message = "Title must be at most 150 characters")
        String title,

        @Size(max = 2000, message = "Description must be at most 2000 characters")
        String description,

        @NotBlank(message = "Genre is required")
        @Size(max = 60, message = "Genre must be at most 60 characters")
        String genre,

        @NotBlank(message = "Language is required")
        @Size(max = 60, message = "Language must be at most 60 characters")
        String language,

        @NotNull(message = "Duration is required")
        @Min(value = 1, message = "Duration must be at least 1 minute")
        @Max(value = 720, message = "Duration cannot exceed 720 minutes")
        Integer durationMinutes,

        @NotNull(message = "Rating is required")
        @DecimalMin(value = "0.0", message = "Rating cannot be less than 0")
        @DecimalMax(value = "10.0", message = "Rating cannot be greater than 10")
        @Digits(integer = 2, fraction = 1, message = "Rating can have at most one decimal place")
        BigDecimal rating,

        @Size(max = 2048, message = "Poster URL must be at most 2048 characters")
        @Pattern(regexp = "^(https?://\\S+)?$", message = "Poster URL must start with http:// or https://")
        String posterUrl,

        @NotNull(message = "Release date is required")
        LocalDate releaseDate) {
}
