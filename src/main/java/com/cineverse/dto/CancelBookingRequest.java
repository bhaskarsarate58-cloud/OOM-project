package com.cineverse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelBookingRequest(
        @NotBlank(message = "Please provide a cancellation reason")
        @Size(max = 500, message = "Cancellation reason must be 500 characters or fewer")
        String reason) {
}
