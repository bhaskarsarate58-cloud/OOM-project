package com.cineverse.dto;

import java.math.BigDecimal;
import java.util.List;

public record DiscoveryMovieDto(
        String id,
        String source,
        Long localMovieId,
        String title,
        String description,
        List<String> genres,
        String language,
        BigDecimal rating,
        String posterUrl,
        String releaseDate,
        boolean bookable) {
}
