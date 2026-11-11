package com.cineverse.dto;

public record ScreenDto(
        Long id,
        String name,
        Integer rowCount,
        Integer seatsPerRow,
        Integer totalSeatCapacity) {
}
