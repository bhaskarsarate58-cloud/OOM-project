package com.cineverse.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import com.cineverse.entity.ShowStatus;

public record ShowRequest(
        @NotNull(message = "Movie ID is required")
        @Positive(message = "Movie ID must be a positive number")
        Long movieId,

        @NotNull(message = "Screen ID is required")
        @Positive(message = "Screen ID must be a positive number")
        Long screenId,

        @NotNull(message = "Show start time is required")
        @Future(message = "Show must start in the future")
        LocalDateTime startTime,

        @NotNull(message = "Show end time is required")
        LocalDateTime endTime,

        @NotNull(message = "Ticket price is required")
        @DecimalMin(value = "0.01", message = "Ticket price must be greater than zero")
        @Digits(integer = 6, fraction = 2, message = "Ticket price can have at most two decimal places")
        BigDecimal ticketPrice,

        @NotNull(message = "Show status is required")
        ShowStatus status) {

    @AssertTrue(message = "Show end time must be later than its start time")
    public boolean isEndTimeAfterStartTime() {
        return startTime == null || endTime == null || endTime.isAfter(startTime);
    }
}
