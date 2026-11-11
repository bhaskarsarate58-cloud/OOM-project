package com.cineverse.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cineverse.entity.BookingSeat;

public interface BookingSeatRepository extends JpaRepository<BookingSeat, Long> {

    @Query("select bookingSeat.seat.id from BookingSeat bookingSeat where bookingSeat.show.id = :showId and bookingSeat.booking.status = com.cineverse.entity.BookingStatus.CONFIRMED")
    List<Long> findBookedSeatIdsByShowId(@Param("showId") Long showId);
}
