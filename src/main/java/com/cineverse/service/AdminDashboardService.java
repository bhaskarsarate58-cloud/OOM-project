package com.cineverse.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cineverse.dto.AdminBookingDto;
import com.cineverse.dto.AdminDashboardDto;
import com.cineverse.entity.AccountRole;
import com.cineverse.entity.Booking;
import com.cineverse.entity.BookingSeat;
import com.cineverse.entity.BookingStatus;
import com.cineverse.entity.CustomerAccount;
import com.cineverse.repository.BookingRepository;
import com.cineverse.repository.CinemaRepository;
import com.cineverse.repository.CustomerAccountRepository;
import com.cineverse.repository.MovieRepository;
import com.cineverse.repository.ShowRepository;

@Service
public class AdminDashboardService {

    private final CustomerAccountRepository customerAccountRepository;
    private final MovieRepository movieRepository;
    private final CinemaRepository cinemaRepository;
    private final ShowRepository showRepository;
    private final BookingRepository bookingRepository;

    public AdminDashboardService(
            CustomerAccountRepository customerAccountRepository,
            MovieRepository movieRepository,
            CinemaRepository cinemaRepository,
            ShowRepository showRepository,
            BookingRepository bookingRepository) {
        this.customerAccountRepository = customerAccountRepository;
        this.movieRepository = movieRepository;
        this.cinemaRepository = cinemaRepository;
        this.showRepository = showRepository;
        this.bookingRepository = bookingRepository;
    }

    @Transactional(readOnly = true)
    public boolean isAdmin(Long accountId) {
        if (accountId == null) return false;
        return customerAccountRepository.findById(accountId)
                .map(account -> account.getRole() == AccountRole.ADMIN)
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public AdminDashboardDto getDashboard(LocalDate from, LocalDate to) {
        List<Booking> bookings;
        if (from != null && to != null) {
            bookings = bookingRepository.findAllByShow_StartTimeGreaterThanEqualAndShow_StartTimeLessThanOrderByShow_StartTimeDesc(
                    from.atStartOfDay(), to.plusDays(1).atStartOfDay());
        } else if (from != null) {
            bookings = bookingRepository.findAllByShow_StartTimeGreaterThanEqualOrderByShow_StartTimeDesc(
                    from.atStartOfDay());
        } else if (to != null) {
            bookings = bookingRepository.findAllByShow_StartTimeLessThanOrderByShow_StartTimeDesc(
                    to.plusDays(1).atStartOfDay());
        } else {
            bookings = bookingRepository.findAllByOrderByCreatedAtDesc();
        }

        List<AdminBookingDto> rows = bookings.stream()
                .map(AdminDashboardService::toDto)
                .toList();
        BigDecimal revenue = bookings.stream()
                .filter(booking -> booking.getStatus() == BookingStatus.CONFIRMED)
                .map(Booking::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long seatsBooked = bookings.stream()
                .filter(booking -> booking.getStatus() == BookingStatus.CONFIRMED)
                .mapToLong(booking -> booking.getBookedSeats().size())
                .sum();
        return new AdminDashboardDto(
                from,
                to,
                movieRepository.count(),
                cinemaRepository.count(),
                showRepository.count(),
                customerAccountRepository.count(),
                bookings.size(),
                seatsBooked,
                revenue,
                counts(bookings, booking -> booking.getShow().getMovie().getTitle()),
                seatCountsByMovie(bookings),
                counts(bookings, booking -> booking.getShow().getScreen().getCinema().getCity()),
                counts(bookings, booking -> booking.getShow().getStartTime().toString()
                        + " · " + booking.getShow().getMovie().getTitle()),
                counts(bookings, booking -> valueOr(booking.getPaymentMethod(), "UNKNOWN")),
                counts(bookings, booking -> booking.getStatus() == BookingStatus.CONFIRMED
                        ? "SIMULATED_SUCCESS" : booking.getStatus().name()),
                counts(bookings.stream()
                        .filter(booking -> booking.getStatus() == BookingStatus.CANCELLED)
                        .toList(), booking -> booking.getStatus().name()),
                counts(bookings.stream()
                        .filter(booking -> booking.getAftercareType() != null)
                        .toList(), booking -> booking.getAftercareType().name() + " · "
                                + valueOr(booking.getAftercareStatus() == null
                                        ? null : booking.getAftercareStatus().name(), "PENDING")),
                rows);
    }

    private static Map<String, Long> counts(List<Booking> bookings, Function<Booking, String> key) {
        Map<String, Long> result = new TreeMap<>();
        bookings.forEach(booking -> result.merge(valueOr(key.apply(booking), "Unknown"), 1L, Long::sum));
        return result;
    }

    private static Map<String, Long> seatCountsByMovie(List<Booking> bookings) {
        Map<String, Long> result = new TreeMap<>();
        bookings.stream()
                .filter(booking -> booking.getStatus() == BookingStatus.CONFIRMED)
                .forEach(booking -> result.merge(
                        booking.getShow().getMovie().getTitle(),
                        (long) booking.getBookedSeats().size(),
                        Long::sum));
        return result;
    }

    private static String valueOr(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private static AdminBookingDto toDto(Booking booking) {
        List<String> seats = booking.getBookedSeats().stream()
                .sorted(Comparator
                        .comparing((BookingSeat seat) -> seat.getSeat().getRowNumber())
                        .thenComparing(seat -> seat.getSeat().getSeatNumber()))
                .map(seat -> seat.getSeat().getLabel())
                .toList();
        CustomerAccount customer = booking.getCustomerAccount();
        return new AdminBookingDto(
                booking.getId(),
                booking.getBookingReference(),
                customer == null ? "Guest" : customer.getDisplayName(),
                customer == null ? "" : customer.getEmail(),
                booking.getShow().getMovie().getTitle(),
                booking.getShow().getScreen().getCinema().getName(),
                booking.getShow().getScreen().getCinema().getCity(),
                booking.getShow().getScreen().getName(),
                booking.getShow().getStartTime(),
                booking.getStatus(),
                seats,
                booking.getTotalPrice(),
                valueOr(booking.getPaymentMethod(), "UNKNOWN"),
                booking.getStatus() == BookingStatus.CONFIRMED ? "SIMULATED_SUCCESS" : booking.getStatus().name(),
                booking.getCancellationReason(),
                booking.getAftercareType(),
                booking.getAftercareStatus());
    }
}
