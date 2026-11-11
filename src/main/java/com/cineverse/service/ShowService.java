package com.cineverse.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cineverse.dto.ShowDto;
import com.cineverse.dto.ShowRequest;
import com.cineverse.entity.Cinema;
import com.cineverse.entity.Movie;
import com.cineverse.entity.Screen;
import com.cineverse.entity.Show;
import com.cineverse.entity.ShowStatus;
import com.cineverse.exception.CinemaNotFoundException;
import com.cineverse.exception.MovieNotFoundException;
import com.cineverse.exception.RequestConflictException;
import com.cineverse.exception.ScreenNotFoundException;
import com.cineverse.exception.ShowNotFoundException;
import com.cineverse.repository.CinemaRepository;
import com.cineverse.repository.MovieRepository;
import com.cineverse.repository.ScreenRepository;
import com.cineverse.repository.ShowRepository;

@Service
public class ShowService {

    private final ShowRepository showRepository;
    private final MovieRepository movieRepository;
    private final ScreenRepository screenRepository;
    private final CinemaRepository cinemaRepository;

    public ShowService(
            ShowRepository showRepository,
            MovieRepository movieRepository,
            ScreenRepository screenRepository,
            CinemaRepository cinemaRepository) {
        this.showRepository = showRepository;
        this.movieRepository = movieRepository;
        this.screenRepository = screenRepository;
        this.cinemaRepository = cinemaRepository;
    }

    @Transactional(readOnly = true)
    public List<ShowDto> getAllShows() {
        return showRepository.findAllByOrderByStartTimeAsc()
                .stream()
                .map(ShowService::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ShowDto getShowById(Long id) {
        return toDto(findShow(id));
    }

    @Transactional
    public ShowDto updateTicketPrice(Long id, BigDecimal ticketPrice) {
        Show show = showRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ShowNotFoundException(id));
        show.setTicketPrice(ticketPrice);
        return toDto(showRepository.save(show));
    }

    @Transactional
    public ShowDto createShow(ShowRequest request) {
        Movie movie = movieRepository.findById(request.movieId())
                .orElseThrow(() -> new MovieNotFoundException(request.movieId()));

        Screen screen = screenRepository.findByIdForUpdate(request.screenId())
                .orElseThrow(() -> new ScreenNotFoundException(request.screenId()));

        if (request.status() == ShowStatus.SCHEDULED
                && showRepository.existsByScreen_IdAndStatusAndStartTimeLessThanAndEndTimeGreaterThan(
                        screen.getId(),
                        ShowStatus.SCHEDULED,
                        request.endTime(),
                        request.startTime())) {
            throw new RequestConflictException(
                    "This screen already has a scheduled show during the requested time");
        }

        Show show = new Show(
                movie,
                screen,
                request.startTime(),
                request.endTime(),
                request.ticketPrice(),
                request.status());
        return toDto(showRepository.save(show));
    }

    @Transactional(readOnly = true)
    public List<ShowDto> getShowsByMovie(Long movieId) {
        if (!movieRepository.existsById(movieId)) {
            throw new MovieNotFoundException(movieId);
        }

        return showRepository.findByMovie_IdOrderByStartTimeAsc(movieId)
                .stream()
                .map(ShowService::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ShowDto> getShowsByCinema(Long cinemaId) {
        if (!cinemaRepository.existsById(cinemaId)) {
            throw new CinemaNotFoundException(cinemaId);
        }

        return showRepository.findByScreen_Cinema_IdOrderByStartTimeAsc(cinemaId)
                .stream()
                .map(ShowService::toDto)
                .toList();
    }

    private Show findShow(Long id) {
        return showRepository.findById(id)
                .orElseThrow(() -> new ShowNotFoundException(id));
    }

    private static ShowDto toDto(Show show) {
        Screen screen = show.getScreen();
        Cinema cinema = screen.getCinema();

        return new ShowDto(
                show.getId(),
                show.getMovie().getId(),
                show.getMovie().getTitle(),
                cinema.getId(),
                cinema.getName(),
                cinema.getCity(),
                screen.getId(),
                screen.getName(),
                screen.getTotalSeatCapacity(),
                show.getStartTime(),
                show.getEndTime(),
                show.getTicketPrice(),
                show.getStatus(),
                cinema.getName().equals("CineVerse Demo Cinema"));
    }
}
