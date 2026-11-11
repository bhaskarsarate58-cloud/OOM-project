package com.cineverse.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.cineverse.entity.BookingStatus;
import com.cineverse.entity.BookingAftercareType;
import com.cineverse.entity.BookingAftercareStatus;

public record AdminBookingDto(
        Long id,
        String bookingReference,
        String customerName,
        String customerEmail,
        String movieTitle,
        String cinemaName,
        String city,
        String screenName,
        LocalDateTime showStartTime,
        BookingStatus status,
        List<String> seatLabels,
        BigDecimal totalPrice,
        String paymentMethod,
        String paymentStatus,
        String cancellationReason,
        BookingAftercareType aftercareType,
        BookingAftercareStatus aftercareStatus) {
}
