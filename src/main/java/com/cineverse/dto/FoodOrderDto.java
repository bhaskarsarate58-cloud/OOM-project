package com.cineverse.dto;

import java.math.BigDecimal;

public record FoodOrderDto(
        String itemId,
        String name,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal totalPrice) {
}