package com.cineverse.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MovieDto(
        Long id,
        String title,
        String description,
        String genre,
        String language,
        Integer durationMinutes,
        BigDecimal rating,
        String posterUrl,
        LocalDate releaseDate) {
}
