package com.cineverse.dto;

import java.util.List;

public record DiscoveryMoviesPageDto(
        List<DiscoveryMovieDto> results,
        int page,
        int totalPages) {
}
