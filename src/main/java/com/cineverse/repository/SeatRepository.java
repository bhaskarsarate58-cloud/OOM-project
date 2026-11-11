package com.cineverse.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cineverse.entity.Seat;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByScreen_IdOrderByRowNumberAscSeatNumberAsc(Long screenId);
}
