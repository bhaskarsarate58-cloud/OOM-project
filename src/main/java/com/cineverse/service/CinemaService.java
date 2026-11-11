package com.cineverse.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cineverse.dto.CinemaDto;
import com.cineverse.dto.CinemaRequest;
import com.cineverse.dto.ScreenDto;
import com.cineverse.entity.Cinema;
import com.cineverse.entity.Screen;
import com.cineverse.exception.CinemaNotFoundException;
import com.cineverse.repository.CinemaRepository;
import com.cineverse.repository.BookingRepository;
import com.cineverse.repository.ScreenRepository;
import com.cineverse.repository.ShowRepository;

@Service
public class CinemaService {

    private final CinemaRepository cinemaRepository;
    private final BookingRepository bookingRepository;
    private final ScreenRepository screenRepository;
    private final ShowRepository showRepository;

    public CinemaService(
            CinemaRepository cinemaRepository,
            BookingRepository bookingRepository,
            ScreenRepository screenRepository,
            ShowRepository showRepository) {
        this.cinemaRepository = cinemaRepository;
        this.bookingRepository = bookingRepository;
        this.screenRepository = screenRepository;
        this.showRepository = showRepository;
    }

    @Transactional(readOnly = true)
    public List<CinemaDto> getAllCinemas() {
        return cinemaRepository.findAllByOrderByCityAscNameAsc()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public CinemaDto getCinemaById(Long id) {
        return toDto(findCinema(id));
    }

    @Transactional
    public CinemaDto createCinema(CinemaRequest request) {
        Cinema cinema = new Cinema(
                request.name().trim(),
                request.address().trim(),
                request.city().trim(),
                cleanFacilities(request.facilities()));
        return toDto(cinemaRepository.save(cinema));
    }

    @Transactional
    public CinemaDto updateCinema(Long id, CinemaRequest request) {
        Cinema cinema = findCinema(id);
        cinema.updateDetails(
                request.name().trim(),
                request.address().trim(),
                request.city().trim(),
                cleanFacilities(request.facilities()));
        return toDto(cinemaRepository.save(cinema));
    }

    @Transactional
    public void deleteCinema(Long id) {
        Cinema cinema = cinemaRepository.findById(id)
                .orElseThrow(() -> new CinemaNotFoundException(id));
        bookingRepository.deleteAllByShow_Screen_Cinema_Id(id);
        showRepository.deleteAllByScreen_Cinema_Id(id);
        screenRepository.deleteAllByCinema_Id(id);
        cinemaRepository.delete(cinema);
    }

    private Cinema findCinema(Long id) {
        return cinemaRepository.findById(id)
                .orElseThrow(() -> new CinemaNotFoundException(id));
    }

    private static List<String> cleanFacilities(List<String> facilities) {
        return facilities.stream()
                .map(String::trim)
                .toList();
    }

    private CinemaDto toDto(Cinema cinema) {
        List<ScreenDto> screens = screenRepository.findByCinema_IdOrderByNameAsc(cinema.getId())
                .stream()
                .map(CinemaService::toScreenDto)
                .toList();

        return new CinemaDto(
                cinema.getId(),
                cinema.getName(),
                cinema.getAddress(),
                cinema.getCity(),
                List.copyOf(cinema.getFacilities()),
                screens);
    }

    private static ScreenDto toScreenDto(Screen screen) {
        return new ScreenDto(
                screen.getId(),
                screen.getName(),
                screen.getRowCount(),
                screen.getSeatsPerRow(),
                screen.getTotalSeatCapacity());
    }
}
