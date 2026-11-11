package com.cineverse.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cineverse.entity.Cinema;

public interface CinemaRepository extends JpaRepository<Cinema, Long> {

    List<Cinema> findAllByOrderByCityAscNameAsc();

    Optional<Cinema> findFirstByNameAndCityOrderByIdAsc(String name, String city);
}
