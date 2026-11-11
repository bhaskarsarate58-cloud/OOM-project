package com.cineverse.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record AdminDashboardDto(
        LocalDate from,
        LocalDate to,
        long totalMovies,
        long totalCinemas,
        long totalShows,
        long totalCustomers,
        long totalBookings,
        long totalSeatsBooked,
        BigDecimal totalRevenue,
        Map<String, Long> bookingsByMovie,
        Map<String, Long> mostBookedMovies,
        Map<String, Long> bookingsByCity,
        Map<String, Long> bookingsByShow,
        Map<String, Long> bookingsByPaymentMethod,
        Map<String, Long> bookingsByPaymentStatus,
        Map<String, Long> cancellations,
        Map<String, Long> refundRequests,
        List<AdminBookingDto> bookings) {
}
