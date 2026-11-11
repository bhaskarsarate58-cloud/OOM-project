package com.cineverse.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

public record UpdateShowPriceRequest(
        @NotNull(message = "Ticket price is required")
        @DecimalMin(value = "0.01", message = "Ticket price must be greater than zero")
        @Digits(integer = 6, fraction = 2, message = "Ticket price can have at most two decimal places")
        BigDecimal ticketPrice) {
}
