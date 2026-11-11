package com.cineverse.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cineverse.entity.Movie;

public interface MovieRepository extends JpaRepository<Movie, Long> {

    List<Movie> findAllByOrderByTitleAsc();

    List<Movie> findByTitleContainingIgnoreCaseOrderByTitleAsc(String title);

    Optional<Movie> findFirstByTitleOrderByIdAsc(String title);
}
