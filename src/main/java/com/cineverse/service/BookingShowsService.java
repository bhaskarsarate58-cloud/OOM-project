package com.cineverse.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cineverse.dto.DiscoveryMovieDto;
import com.cineverse.dto.ShowDto;
import com.cineverse.entity.Cinema;
import com.cineverse.entity.Movie;
import com.cineverse.entity.Screen;
import com.cineverse.entity.Show;
import com.cineverse.entity.ShowStatus;
import com.cineverse.exception.InvalidRequestException;
import com.cineverse.exception.ResourceNotFoundException;
import com.cineverse.repository.CinemaRepository;
import com.cineverse.repository.MovieRepository;
import com.cineverse.repository.ScreenRepository;
import com.cineverse.repository.ShowRepository;

@Service
public class BookingShowsService {

    private static final List<String> DEMO_CINEMA_NAMES = List.of(
            "CineVerse Demo Cinema", "CineVerse Demo Cinema 2", "CineVerse Demo Cinema 3");
    private static final Set<String> CITIES = Set.of(
            "Ahmedabad", "Bengaluru", "Bhopal", "Bhubaneswar", "Chandigarh", "Chennai",
            "Coimbatore", "Dehradun", "Delhi", "Goa", "Guwahati", "Hyderabad", "Indore",
            "Jaipur", "Kanpur", "Kochi", "Kolkata", "Lucknow", "Mumbai", "Nagpur",
            "Patna", "Prayagraj", "Pune", "Ranchi", "Surat", "Varanasi", "Visakhapatnam");
    private static final List<LocalTime> DEMO_START_TIMES = List.of(
            LocalTime.of(11, 0), LocalTime.of(15, 0), LocalTime.of(19, 0));
    private static final Map<String, CuratedMovie> CURATED_MOVIES = Map.of(
            "three-idiots", new CuratedMovie("3 Idiots", "Comedy · Drama", "Hindi", "2009-12-25",
                    "https://image.tmdb.org/t/p/w500/66A9MqXOyVFCssoloscw79z8Tew.jpg", new BigDecimal("8.0")),
            "rrr", new CuratedMovie("RRR", "Action · Drama", "Telugu", "2022-03-25",
                    "https://image.tmdb.org/t/p/w500/wE0I6efAW4cDDmZQWtwZMOW44EJ.jpg", new BigDecimal("7.8")),
            "inception", new CuratedMovie("Inception", "Science Fiction", "English", "2010-07-16",
                    "https://image.tmdb.org/t/p/w500/oYuLEt3zVCKq57qu2F8dT7NIa6f.jpg", new BigDecimal("8.4")),
            "interstellar", new CuratedMovie("Interstellar", "Science Fiction", "English", "2014-11-07",
                    "https://image.tmdb.org/t/p/w500/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg", new BigDecimal("8.4")),
            "dark-knight", new CuratedMovie("The Dark Knight", "Action", "English", "2008-07-18",
                    "https://image.tmdb.org/t/p/w500/qJ2tW6WMUDux911r6m7haRef0WH.jpg", new BigDecimal("8.5")));

    private final MovieRepository movieRepository;
    private final CinemaRepository cinemaRepository;
    private final ScreenRepository screenRepository;
    private final ShowRepository showRepository;
    private final TmdbDiscoveryService discoveryService;

    public BookingShowsService(
            MovieRepository movieRepository,
            CinemaRepository cinemaRepository,
            ScreenRepository screenRepository,
            ShowRepository showRepository,
            TmdbDiscoveryService discoveryService) {
        this.movieRepository = movieRepository;
        this.cinemaRepository = cinemaRepository;
        this.screenRepository = screenRepository;
        this.showRepository = showRepository;
        this.discoveryService = discoveryService;
    }

    @Transactional
    public synchronized List<ShowDto> getBookingShows(String source, String id, String requestedCity, LocalDate date) {
        String city = CITIES.stream()
                .filter(value -> value.equalsIgnoreCase(requestedCity == null ? "" : requestedCity.trim()))
                .findFirst()
                .orElseThrow(() -> new InvalidRequestException(
                        "Choose a supported Indian city",
                        Map.of("city", "Select a city from the CineVerse city list")));
        LocalDate today = LocalDate.now();
        if (date == null || date.isBefore(today) || date.isAfter(today.plusDays(7))) {
            throw new InvalidRequestException(
                    "Choose a show date within the next seven days",
                    Map.of("date", "Show dates are available from today through seven days ahead"));
        }

        Movie movie = resolveMovie(source, id);
        List<Show> cityShows = showRepository.findByMovie_IdOrderByStartTimeAsc(movie.getId()).stream()
                .filter(show -> show.getStatus() == ShowStatus.SCHEDULED
                        && show.getStartTime().isAfter(LocalDateTime.now())
                        && show.getScreen().getCinema().getCity().equalsIgnoreCase(city))
                .toList();
        List<Show> configuredShows = cityShows.stream()
                .filter(show -> !isDemoCinema(show) && show.getStartTime().toLocalDate().equals(date))
                .toList();
        List<Show> existingDemoShows = cityShows.stream()
                .filter(BookingShowsService::isDemoCinema)
                .filter(show -> show.getStartTime().toLocalDate().equals(date))
                .toList();
        List<Show> bookingShows = new ArrayList<>(configuredShows);
        bookingShows.addAll(existingDemoShows);
        Set<Long> scheduledCinemaIds = bookingShows.stream()
                .map(show -> show.getScreen().getCinema().getId())
                .collect(Collectors.toSet());

        LocalDateTime now = LocalDateTime.now();
        List<LocalTime> startTimes = DEMO_START_TIMES.stream()
                .filter(startTime -> LocalDateTime.of(date, startTime).isAfter(now))
                .toList();
        if (startTimes.isEmpty() && date.equals(today)) {
            LocalTime nextHour = now.toLocalTime().withMinute(0).withSecond(0).withNano(0).plusHours(1);
            if (!nextHour.equals(LocalTime.MIDNIGHT)) {
                startTimes = List.of(nextHour);
            }
        }
        int demoCinemaIndex = 0;
        for (String demoCinemaName : DEMO_CINEMA_NAMES) {
            if (scheduledCinemaIds.size() >= 3 || startTimes.isEmpty()) {
                break;
            }
            Cinema cinema = cinemaRepository.findFirstByNameAndCityOrderByIdAsc(demoCinemaName, city)
                    .orElseGet(() -> cinemaRepository.save(new Cinema(
                            demoCinemaName,
                            "Simulation inventory only — no real theatre",
                            city,
                            List.of("Demo inventory"))));
            if (!scheduledCinemaIds.add(cinema.getId())) {
                continue;
            }
            String screenName = "Demo Screen " + movie.getId();
            Screen screen = screenRepository.findByCinema_IdAndName(cinema.getId(), screenName)
                    .orElseGet(() -> screenRepository.save(new Screen(screenName, 6, 10, cinema)));
            LocalTime startTime = startTimes.get(demoCinemaIndex % startTimes.size());
            demoCinemaIndex++;
            LocalDateTime startsAt = LocalDateTime.of(date, startTime);
            bookingShows.add(showRepository.save(new Show(
                    movie,
                    screen,
                    startsAt,
                    startsAt.plusHours(2),
                    new BigDecimal("199.00"),
                    ShowStatus.SCHEDULED)));
        }
        return bookingShows.stream()
                .map(BookingShowsService::toDto)
                .toList();
    }

    private Movie resolveMovie(String source, String id) {
        if ("local".equalsIgnoreCase(source)) {
            try {
                return movieRepository.findById(Long.parseLong(id))
                        .orElseThrow(() -> new ResourceNotFoundException("Movie", Long.parseLong(id)));
            } catch (NumberFormatException exception) {
                throw new InvalidRequestException("Invalid local movie ID", Map.of("movieId", "Choose a listed movie"));
            }
        }
        if ("curated".equalsIgnoreCase(source)) {
            CuratedMovie curated = CURATED_MOVIES.get(id == null ? "" : id.toLowerCase(Locale.ROOT));
            if (curated == null) {
                throw new InvalidRequestException(
                        "Unknown curated movie",
                        Map.of("movieId", "Choose a movie from the CineVerse fallback catalogue"));
            }
            return movieRepository.findFirstByTitleOrderByIdAsc(curated.title()).orElseGet(() ->
                    movieRepository.save(new Movie(
                            curated.title(),
                            "Curated CineVerse fallback title. Showtimes shown here are demo-only unless configured locally.",
                            curated.genre(),
                            curated.language(),
                            120,
                            curated.rating(),
                            curated.posterUrl(),
                            LocalDate.parse(curated.releaseDate()))));
        }
        if (!"tmdb".equalsIgnoreCase(source)) {
            throw new InvalidRequestException("Unknown movie source", Map.of("source", "Choose local or TMDB"));
        }
        long tmdbId;
        try {
            tmdbId = Long.parseLong(id);
        } catch (NumberFormatException exception) {
            throw new InvalidRequestException("Invalid TMDB movie ID", Map.of("movieId", "Choose a live movie result"));
        }
        DiscoveryMovieDto discovered = discoveryService.getMovie(tmdbId);
        if (discovered.localMovieId() != null) {
            return movieRepository.findById(discovered.localMovieId())
                    .orElseThrow(() -> new ResourceNotFoundException("Movie", discovered.localMovieId()));
        }
        Movie movie = movieRepository.findFirstByTitleOrderByIdAsc(discovered.title()).orElse(null);
        if (movie != null) {
            return movie;
        }
        LocalDate releaseDate = parseReleaseDate(discovered.releaseDate());
        String genre = discovered.genres().isEmpty() ? "Film" : discovered.genres().stream()
                .limit(3)
                .collect(Collectors.joining(" · "));
        BigDecimal rating = discovered.rating() == null
                ? BigDecimal.ZERO
                : discovered.rating().max(BigDecimal.ZERO).min(new BigDecimal("10.0"))
                        .setScale(1, java.math.RoundingMode.HALF_UP);
        return movieRepository.save(new Movie(
                discovered.title(),
                shorten(discovered.description(), 2000),
                shorten(genre, 60),
                languageName(discovered.language()),
                120,
                rating,
                discovered.posterUrl(),
                releaseDate));
    }

    private static LocalDate parseReleaseDate(String releaseDate) {
        try {
            return releaseDate == null || releaseDate.isBlank() ? LocalDate.now() : LocalDate.parse(releaseDate);
        } catch (java.time.DateTimeException exception) {
            return LocalDate.now();
        }
    }

    private static String shorten(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private static String languageName(String code) {
        return switch (code == null ? "" : code.toLowerCase(Locale.ROOT)) {
            case "ar" -> "Arabic";
            case "bn" -> "Bengali";
            case "bho" -> "Bhojpuri";
            case "en" -> "English";
            case "es" -> "Spanish";
            case "gu" -> "Gujarati";
            case "hi" -> "Hindi";
            case "ja" -> "Japanese";
            case "kn" -> "Kannada";
            case "ko" -> "Korean";
            case "ml" -> "Malayalam";
            case "mr" -> "Marathi";
            case "pa" -> "Punjabi";
            case "ta" -> "Tamil";
            case "te" -> "Telugu";
            case "ur" -> "Urdu";
            default -> code == null || code.isBlank() ? "Other" : code;
        };
    }

    private static ShowDto toDto(Show show) {
        Cinema cinema = show.getScreen().getCinema();
        return new ShowDto(
                show.getId(),
                show.getMovie().getId(),
                show.getMovie().getTitle(),
                cinema.getId(),
                cinema.getName(),
                cinema.getCity(),
                show.getScreen().getId(),
                show.getScreen().getName(),
                show.getScreen().getTotalSeatCapacity(),
                show.getStartTime(),
                show.getEndTime(),
                show.getTicketPrice(),
                show.getStatus(),
                isDemoCinema(show));
    }

    private static boolean isDemoCinema(Show show) {
                return DEMO_CINEMA_NAMES.contains(show.getScreen().getCinema().getName());
    }

    private record CuratedMovie(
            String title,
            String genre,
            String language,
            String releaseDate,
            String posterUrl,
            BigDecimal rating) {
    }
}
