package com.cineverse.dto;

import java.math.BigDecimal;

public record FoodMenuItemDto(
        String itemId,
        String name,
        String description,
        BigDecimal unitPrice) {
}