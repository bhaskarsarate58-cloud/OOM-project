package com.cineverse.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record FoodOrderRequest(
        @NotBlank(message = "Choose a listed food item")
        @Pattern(regexp = "popcorn|cola|nachos|combo", message = "Choose a listed food item")
        String itemId,

        @Min(value = 1, message = "Food quantity must be at least one")
        @Max(value = 10, message = "Food quantity cannot exceed ten")
        int quantity) {
}