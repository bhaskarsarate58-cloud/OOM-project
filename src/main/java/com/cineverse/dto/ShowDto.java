package com.cineverse.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.cineverse.entity.ShowStatus;

public record ShowDto(
        Long id,
        Long movieId,
        String movieTitle,
        Long cinemaId,
        String cinemaName,
        String city,
        Long screenId,
        String screenName,
        Integer totalSeatCapacity,
        LocalDateTime startTime,
        LocalDateTime endTime,
        BigDecimal ticketPrice,
        ShowStatus status,
        boolean demo) {
}
