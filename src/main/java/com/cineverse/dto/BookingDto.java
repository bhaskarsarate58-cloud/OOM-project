package com.cineverse.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.cineverse.entity.BookingStatus;
import com.cineverse.entity.BookingAftercareType;
import com.cineverse.entity.BookingAftercareStatus;

public record BookingDto(
        Long id,
        String bookingReference,
        BookingStatus status,
        Long showId,
        String movieTitle,
        String cinemaName,
        String city,
        String screenName,
        LocalDateTime startTime,
        List<String> seatLabels,
        BigDecimal totalPrice,
        LocalDateTime createdAt,
        String cancellationReason,
        LocalDateTime cancelledAt,
        BookingAftercareType aftercareType,
        BookingAftercareStatus aftercareStatus,
        LocalDateTime aftercareRequestedAt,
        String paymentMethod,
        String paymentStatus,
        String cinemaAddress,
        BigDecimal ticketPrice,
        BigDecimal seatSubtotal,
        BigDecimal foodSubtotal,
        List<FoodOrderDto> foodItems) {
}
