package com.cineverse.repository;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cineverse.entity.Screen;

public interface ScreenRepository extends JpaRepository<Screen, Long> {

    List<Screen> findByCinema_IdOrderByNameAsc(Long cinemaId);

    Optional<Screen> findByCinema_IdAndName(Long cinemaId, String name);

    void deleteAllByCinema_Id(Long cinemaId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select screen from Screen screen where screen.id = :id")
    Optional<Screen> findByIdForUpdate(@Param("id") Long id);
}
