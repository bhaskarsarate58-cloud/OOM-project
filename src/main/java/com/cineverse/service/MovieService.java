package com.cineverse.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cineverse.dto.MovieDto;
import com.cineverse.dto.MovieRequest;
import com.cineverse.entity.Movie;
import com.cineverse.exception.MovieNotFoundException;
import com.cineverse.exception.RequestConflictException;
import com.cineverse.repository.MovieRepository;
import com.cineverse.repository.ShowRepository;

@Service
public class MovieService {

    private final MovieRepository movieRepository;
    private final ShowRepository showRepository;

    public MovieService(MovieRepository movieRepository, ShowRepository showRepository) {
        this.movieRepository = movieRepository;
        this.showRepository = showRepository;
    }

    @Transactional(readOnly = true)
    public List<MovieDto> getAllMovies() {
        return movieRepository.findAllByOrderByTitleAsc()
                .stream()
                .map(MovieService::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public MovieDto getMovieById(Long id) {
        return toDto(findMovie(id));
    }

    @Transactional
    public MovieDto createMovie(MovieRequest request) {
        Movie movie = new Movie(
                request.title().trim(),
                request.description(),
                request.genre().trim(),
                request.language().trim(),
                request.durationMinutes(),
                request.rating(),
                request.posterUrl(),
                request.releaseDate());
        return toDto(movieRepository.save(movie));
    }

    @Transactional
    public MovieDto updateMovie(Long id, MovieRequest request) {
        Movie movie = findMovie(id);
        movie.updateDetails(
                request.title().trim(),
                request.description(),
                request.genre().trim(),
                request.language().trim(),
                request.durationMinutes(),
                request.rating(),
                request.posterUrl(),
                request.releaseDate());
        return toDto(movieRepository.save(movie));
    }

    @Transactional
    public void deleteMovie(Long id) {
        Movie movie = findMovie(id);
        if (showRepository.existsByMovie_Id(id)) {
            throw new RequestConflictException("Movie cannot be deleted while it has scheduled shows");
        }
        movieRepository.delete(movie);
    }

    @Transactional(readOnly = true)
    public List<MovieDto> searchMoviesByTitle(String title) {
        return movieRepository.findByTitleContainingIgnoreCaseOrderByTitleAsc(title.trim())
                .stream()
                .map(MovieService::toDto)
                .toList();
    }

    private Movie findMovie(Long id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new MovieNotFoundException(id));
    }

    private static MovieDto toDto(Movie movie) {
        return new MovieDto(
                movie.getId(),
                movie.getTitle(),
                movie.getDescription(),
                movie.getGenre(),
                movie.getLanguage(),
                movie.getDurationMinutes(),
                movie.getRating(),
                movie.getPosterUrl(),
                movie.getReleaseDate());
    }
}
