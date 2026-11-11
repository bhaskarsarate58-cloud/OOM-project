package com.cineverse.dto;

import com.cineverse.entity.BookingAftercareType;

import jakarta.validation.constraints.NotNull;

public record BookingAftercareRequest(
        @NotNull(message = "Choose a demo request type")
        BookingAftercareType type) {
}
