package com.cineverse.config;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.cineverse.entity.Cinema;
import com.cineverse.entity.Movie;
import com.cineverse.entity.Screen;
import com.cineverse.entity.Show;
import com.cineverse.entity.ShowStatus;
import com.cineverse.exception.CinemaNotFoundException;
import com.cineverse.exception.MovieNotFoundException;
import com.cineverse.exception.ScreenNotFoundException;
import com.cineverse.repository.CinemaRepository;
import com.cineverse.repository.MovieRepository;
import com.cineverse.repository.ScreenRepository;
import com.cineverse.repository.ShowRepository;

@Component
public class DevelopmentMovieDataLoader implements ApplicationRunner {

    private final MovieRepository movieRepository;
    private final CinemaRepository cinemaRepository;
    private final ScreenRepository screenRepository;
    private final ShowRepository showRepository;

    public DevelopmentMovieDataLoader(
            MovieRepository movieRepository,
            CinemaRepository cinemaRepository,
            ScreenRepository screenRepository,
            ShowRepository showRepository) {
        this.movieRepository = movieRepository;
        this.cinemaRepository = cinemaRepository;
        this.screenRepository = screenRepository;
        this.showRepository = showRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedMovies();
        seedCinemas();
        seedScreens();
        seedShows();
    }

    private void seedMovies() {
        if (movieRepository.count() > 0) {
            return;
        }

        movieRepository.saveAll(List.of(
                new Movie(
                        "Inception",
                        "A skilled thief enters people's dreams to steal secrets and is offered a chance at redemption.",
                        "Science Fiction",
                        "English",
                        148,
                        new BigDecimal("8.8"),
                        "https://image.tmdb.org/t/p/w500/oYuLEt3zVCKq57qu2F8dT7NIa6f.jpg",
                        LocalDate.of(2010, 7, 16)),
                new Movie(
                        "The Dark Knight",
                        "Batman faces the Joker, a criminal mastermind who brings chaos to Gotham City.",
                        "Action",
                        "English",
                        152,
                        new BigDecimal("9.0"),
                        "https://image.tmdb.org/t/p/w500/qJ2tW6WMUDux911r6m7haRef0WH.jpg",
                        LocalDate.of(2008, 7, 18)),
                new Movie(
                        "Interstellar",
                        "A team of explorers travels beyond this galaxy to discover whether mankind has a future among the stars.",
                        "Science Fiction",
                        "English",
                        169,
                        new BigDecimal("8.7"),
                        "https://image.tmdb.org/t/p/w500/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg",
                        LocalDate.of(2014, 11, 7))));
    }

    private void seedCinemas() {
        if (cinemaRepository.count() > 0) {
            return;
        }

        cinemaRepository.saveAll(List.of(
                new Cinema(
                        "CineVerse Central",
                        "12 MG Road",
                        "Bengaluru",
                        List.of("Dolby Atmos", "Wheelchair access", "Snack bar")),
                new Cinema(
                        "CineVerse Lakeside",
                        "48 Marine Drive",
                        "Mumbai",
                        List.of("4K projection", "Parking", "Snack bar"))));
    }

    private void seedScreens() {
        if (screenRepository.count() > 0) {
            return;
        }

        Cinema central = cinemaRepository.findFirstByNameAndCityOrderByIdAsc("CineVerse Central", "Bengaluru")
                .orElseThrow(() -> new CinemaNotFoundException(-1L));
        Cinema lakeside = cinemaRepository.findFirstByNameAndCityOrderByIdAsc("CineVerse Lakeside", "Mumbai")
                .orElseThrow(() -> new CinemaNotFoundException(-1L));

        screenRepository.saveAll(List.of(
                new Screen("Screen 1", 10, 12, central),
                new Screen("Screen 2", 8, 10, central),
                new Screen("Screen 1", 9, 12, lakeside)));
    }

    private void seedShows() {
        if (showRepository.count() > 0) {
            return;
        }

        Movie inception = movieRepository.findFirstByTitleOrderByIdAsc("Inception")
                .orElseThrow(() -> new MovieNotFoundException(-1L));
        Movie interstellar = movieRepository.findFirstByTitleOrderByIdAsc("Interstellar")
                .orElseThrow(() -> new MovieNotFoundException(-1L));
        Movie darkKnight = movieRepository.findFirstByTitleOrderByIdAsc("The Dark Knight")
                .orElseThrow(() -> new MovieNotFoundException(-1L));

        Cinema central = cinemaRepository.findFirstByNameAndCityOrderByIdAsc("CineVerse Central", "Bengaluru")
                .orElseThrow(() -> new CinemaNotFoundException(-1L));
        Cinema lakeside = cinemaRepository.findFirstByNameAndCityOrderByIdAsc("CineVerse Lakeside", "Mumbai")
                .orElseThrow(() -> new CinemaNotFoundException(-1L));

        Screen centralScreenOne = screenRepository.findByCinema_IdAndName(central.getId(), "Screen 1")
                .orElseThrow(() -> new ScreenNotFoundException(-1L));
        Screen centralScreenTwo = screenRepository.findByCinema_IdAndName(central.getId(), "Screen 2")
                .orElseThrow(() -> new ScreenNotFoundException(-1L));
        Screen lakesideScreenOne = screenRepository.findByCinema_IdAndName(lakeside.getId(), "Screen 1")
                .orElseThrow(() -> new ScreenNotFoundException(-1L));

        LocalDate showDate = LocalDate.now().plusDays(7);
        LocalDate secondShowDate = showDate.plusDays(1);
        LocalDate thirdShowDate = showDate.plusDays(2);
        LocalDateTime firstStart = showDate.atTime(18, 0);
        LocalDateTime secondStart = showDate.atTime(21, 0);
        LocalDateTime nextDayStart = showDate.plusDays(1).atTime(19, 0);

        showRepository.saveAll(List.of(
                new Show(
                        inception,
                        centralScreenOne,
                        firstStart,
                        firstStart.plusMinutes(148),
                        new BigDecimal("350.00"),
                        ShowStatus.SCHEDULED),
                new Show(
                        interstellar,
                        centralScreenOne,
                        secondStart,
                        secondStart.plusMinutes(169),
                        new BigDecimal("400.00"),
                        ShowStatus.SCHEDULED),
                new Show(
                        inception,
                        centralScreenTwo,
                        firstStart,
                        firstStart.plusMinutes(148),
                        new BigDecimal("300.00"),
                        ShowStatus.SCHEDULED),
                new Show(
                        inception,
                        centralScreenOne,
                        secondShowDate.atTime(18, 0),
                        secondShowDate.atTime(18, 0).plusMinutes(148),
                        new BigDecimal("350.00"),
                        ShowStatus.SCHEDULED),
                new Show(
                        inception,
                        centralScreenTwo,
                        secondShowDate.atTime(21, 0),
                        secondShowDate.atTime(21, 0).plusMinutes(148),
                        new BigDecimal("300.00"),
                        ShowStatus.SCHEDULED),
                new Show(
                        interstellar,
                        centralScreenTwo,
                        secondStart,
                        secondStart.plusMinutes(169),
                        new BigDecimal("350.00"),
                        ShowStatus.SCHEDULED),
                new Show(
                        interstellar,
                        centralScreenOne,
                        secondShowDate.atTime(21, 0),
                        secondShowDate.atTime(21, 0).plusMinutes(169),
                        new BigDecimal("400.00"),
                        ShowStatus.SCHEDULED),
                new Show(
                        interstellar,
                        centralScreenTwo,
                        secondShowDate.atTime(18, 0),
                        secondShowDate.atTime(18, 0).plusMinutes(169),
                        new BigDecimal("350.00"),
                        ShowStatus.SCHEDULED),
                new Show(
                        darkKnight,
                        lakesideScreenOne,
                        nextDayStart,
                        nextDayStart.plusMinutes(152),
                        new BigDecimal("300.00"),
                        ShowStatus.SCHEDULED),
                new Show(
                        darkKnight,
                        lakesideScreenOne,
                        thirdShowDate.atTime(19, 0),
                        thirdShowDate.atTime(19, 0).plusMinutes(152),
                        new BigDecimal("300.00"),
                        ShowStatus.SCHEDULED)));
    }
}
