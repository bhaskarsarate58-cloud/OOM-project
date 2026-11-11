package com.cineverse.dto;

public record SeatAvailabilityDto(
        String label,
        Integer rowNumber,
        Integer seatNumber,
        boolean available) {
}
