package com.cineverse.controller;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cineverse.dto.BookingAftercareRequest;
import com.cineverse.dto.BookingDto;
import com.cineverse.dto.CancelBookingRequest;
import com.cineverse.dto.CreateBookingRequest;
import com.cineverse.dto.FoodMenuItemDto;
import com.cineverse.dto.SeatAvailabilityDto;
import com.cineverse.service.BookingService;
import com.cineverse.service.CustomerSession;
import com.cineverse.exception.AuthenticationRequiredException;

@Validated
@RestController
@RequestMapping("/api")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/shows/{showId}/seats")
    public List<SeatAvailabilityDto> getShowSeats(@PathVariable @Positive Long showId) {
        return bookingService.getSeatsForShow(showId);
    }

    @GetMapping("/booking-food-menu")
    public List<FoodMenuItemDto> getFoodMenu() {
        return bookingService.getFoodMenu();
    }

    @PostMapping("/bookings")
    public ResponseEntity<BookingDto> createBooking(
            @Valid @RequestBody CreateBookingRequest request,
            HttpServletRequest servletRequest) {
        Long accountId = CustomerSession.accountId(servletRequest.getSession(false));
        if (accountId == null) {
            throw new AuthenticationRequiredException();
        }
        BookingDto booking = bookingService.createBooking(
                request,
                accountId);
        return ResponseEntity.created(URI.create("/api/bookings/" + booking.id())).body(booking);
    }

    @GetMapping("/bookings/{bookingId}")
    public BookingDto getBooking(@PathVariable @Positive Long bookingId, HttpServletRequest request) {
        return bookingService.getBooking(
                bookingId,
                CustomerSession.accountId(request.getSession(false)));
    }

    @PostMapping("/bookings/{bookingId}/cancel")
    public BookingDto cancelBooking(
            @PathVariable @Positive Long bookingId,
            @Valid @RequestBody CancelBookingRequest request,
            HttpServletRequest servletRequest) {
        return bookingService.cancelBooking(
                bookingId,
                CustomerSession.accountId(servletRequest.getSession(false)),
                request.reason());
    }

    @PostMapping("/bookings/{bookingId}/aftercare")
    public BookingDto requestAftercare(
            @PathVariable @Positive Long bookingId,
            @Valid @RequestBody BookingAftercareRequest request,
            HttpServletRequest servletRequest) {
        return bookingService.requestAftercare(
                bookingId,
                CustomerSession.accountId(servletRequest.getSession(false)),
                request.type());
    }

    @DeleteMapping("/bookings/{bookingId}")
    public ResponseEntity<Void> removeCancelledBooking(
            @PathVariable @Positive Long bookingId,
            HttpServletRequest servletRequest) {
        bookingService.removeCancelledBooking(
                bookingId,
                CustomerSession.accountId(servletRequest.getSession(false)));
        return ResponseEntity.noContent().build();
    }
}
