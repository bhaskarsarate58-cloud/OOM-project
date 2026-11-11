package com.cineverse.repository;

import java.util.Optional;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import com.cineverse.entity.Booking;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    java.util.List<Booking> findByCustomerAccount_IdOrderByCreatedAtDesc(Long customerAccountId);

    @EntityGraph(attributePaths = {
            "customerAccount", "show", "show.movie", "show.screen", "show.screen.cinema",
            "bookedSeats", "bookedSeats.seat"
    })
    List<Booking> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {
            "customerAccount", "show", "show.movie", "show.screen", "show.screen.cinema",
            "bookedSeats", "bookedSeats.seat"
    })
    List<Booking> findAllByShow_StartTimeGreaterThanEqualAndShow_StartTimeLessThanOrderByShow_StartTimeDesc(
            LocalDateTime from, LocalDateTime to);

    @EntityGraph(attributePaths = {
            "customerAccount", "show", "show.movie", "show.screen", "show.screen.cinema",
            "bookedSeats", "bookedSeats.seat"
    })
    List<Booking> findAllByShow_StartTimeGreaterThanEqualOrderByShow_StartTimeDesc(LocalDateTime from);

    @EntityGraph(attributePaths = {
            "customerAccount", "show", "show.movie", "show.screen", "show.screen.cinema",
            "bookedSeats", "bookedSeats.seat"
    })
    List<Booking> findAllByShow_StartTimeLessThanOrderByShow_StartTimeDesc(LocalDateTime to);

    @Override
    @EntityGraph(attributePaths = {
            "show", "show.movie", "show.screen", "show.screen.cinema", "bookedSeats", "bookedSeats.seat"
    })
    Optional<Booking> findById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {
            "customerAccount", "show", "show.movie", "show.screen", "show.screen.cinema",
            "bookedSeats", "bookedSeats.seat"
    })
    @Query("select booking from Booking booking where booking.id = :bookingId")
    Optional<Booking> findByIdForUpdate(@Param("bookingId") Long bookingId);

    void deleteAllByShow_Screen_Cinema_Id(Long cinemaId);
}
