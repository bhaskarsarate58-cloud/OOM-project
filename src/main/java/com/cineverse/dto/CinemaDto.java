package com.cineverse.dto;

import java.util.List;

public record CinemaDto(
        Long id,
        String name,
        String address,
        String city,
        List<String> facilities,
        List<ScreenDto> screens) {
}
