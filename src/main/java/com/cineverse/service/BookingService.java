package com.cineverse.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cineverse.dto.BookingDto;
import com.cineverse.dto.CreateBookingRequest;
import com.cineverse.dto.FoodMenuItemDto;
import com.cineverse.dto.FoodOrderDto;
import com.cineverse.dto.FoodOrderRequest;
import com.cineverse.dto.SeatAvailabilityDto;
import com.cineverse.entity.Booking;
import com.cineverse.entity.BookingAftercareType;
import com.cineverse.entity.BookingFoodItem;
import com.cineverse.entity.BookingSeat;
import com.cineverse.entity.CustomerAccount;
import com.cineverse.entity.Seat;
import com.cineverse.entity.Show;
import com.cineverse.entity.ShowStatus;
import com.cineverse.exception.InvalidRequestException;
import com.cineverse.exception.RequestConflictException;
import com.cineverse.exception.AuthenticationRequiredException;
import com.cineverse.exception.ShowNotFoundException;
import com.cineverse.repository.BookingRepository;
import com.cineverse.repository.BookingSeatRepository;
import com.cineverse.repository.CustomerAccountRepository;
import com.cineverse.repository.SeatRepository;
import com.cineverse.repository.ShowRepository;

@Service
public class BookingService {

    private static final BigDecimal PREMIUM_MULTIPLIER = new BigDecimal("1.25");
    private static final List<FoodMenuItemDto> FOOD_MENU = List.of(
            new FoodMenuItemDto("popcorn", "Classic popcorn", "Freshly popped, salted", new BigDecimal("120.00")),
            new FoodMenuItemDto("cola", "Cola", "Chilled 500 ml", new BigDecimal("80.00")),
            new FoodMenuItemDto("nachos", "Nachos", "Cheese dip included", new BigDecimal("180.00")),
            new FoodMenuItemDto("combo", "Movie combo", "Popcorn and two colas", new BigDecimal("250.00")));

    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final SeatRepository seatRepository;
    private final ShowRepository showRepository;
    private final CustomerAccountRepository customerAccountRepository;

    public BookingService(
            BookingRepository bookingRepository,
            BookingSeatRepository bookingSeatRepository,
            SeatRepository seatRepository,
            ShowRepository showRepository,
            CustomerAccountRepository customerAccountRepository) {
        this.bookingRepository = bookingRepository;
        this.bookingSeatRepository = bookingSeatRepository;
        this.seatRepository = seatRepository;
        this.showRepository = showRepository;
        this.customerAccountRepository = customerAccountRepository;
    }

    @Transactional(readOnly = true)
    public List<SeatAvailabilityDto> getSeatsForShow(Long showId) {
        Show show = showRepository.findById(showId).orElseThrow(() -> new ShowNotFoundException(showId));
        Set<Long> bookedSeatIds = new HashSet<>(bookingSeatRepository.findBookedSeatIdsByShowId(showId));

        return seatRepository.findByScreen_IdOrderByRowNumberAscSeatNumberAsc(show.getScreen().getId())
                .stream()
                .map(seat -> new SeatAvailabilityDto(
                        seat.getLabel(),
                        seat.getRowNumber(),
                        seat.getSeatNumber(),
                        !bookedSeatIds.contains(seat.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FoodMenuItemDto> getFoodMenu() {
        return FOOD_MENU;
    }

    @Transactional
    public BookingDto createBooking(CreateBookingRequest request) {
        return createBooking(request, null);
    }

    @Transactional
    public BookingDto createBooking(CreateBookingRequest request, Long customerAccountId) {
        Show show = showRepository.findByIdForUpdate(request.showId())
                .orElseThrow(() -> new ShowNotFoundException(request.showId()));
        if (show.getStatus() != ShowStatus.SCHEDULED || !show.getStartTime().isAfter(LocalDateTime.now())) {
            throw new RequestConflictException("This show is no longer available for booking");
        }

        List<Seat> screenSeats = seatRepository
                .findByScreen_IdOrderByRowNumberAscSeatNumberAsc(show.getScreen().getId());
        Map<String, Seat> seatsByLabel = screenSeats.stream()
                .collect(java.util.stream.Collectors.toMap(Seat::getLabel, seat -> seat));
        List<Seat> selectedSeats = request.seatLabels().stream()
                .map(seatsByLabel::get)
                .toList();

        if (selectedSeats.stream().anyMatch(seat -> seat == null)) {
            Set<String> validLabels = seatsByLabel.keySet();
            List<String> invalidLabels = request.seatLabels().stream()
                    .filter(label -> !validLabels.contains(label))
                    .toList();
            throw new InvalidRequestException(
                    "One or more selected seats do not exist in this screen",
                    Map.of("seatLabels", "Unknown seat label(s): " + String.join(", ", invalidLabels)));
        }

        Set<Long> bookedSeatIds = new HashSet<>(bookingSeatRepository.findBookedSeatIdsByShowId(show.getId()));
        List<String> unavailableLabels = selectedSeats.stream()
                .filter(seat -> bookedSeatIds.contains(seat.getId()))
                .map(Seat::getLabel)
                .toList();
        if (!unavailableLabels.isEmpty()) {
            throw new RequestConflictException(
                    "These seats are no longer available for this show: " + String.join(", ", unavailableLabels));
        }

        BigDecimal seatSubtotal = selectedSeats.stream()
            .map(seat -> seat.getRowNumber() <= 2
                ? show.getTicketPrice().multiply(PREMIUM_MULTIPLIER).setScale(2, RoundingMode.HALF_UP)
                : show.getTicketPrice().setScale(2, RoundingMode.HALF_UP))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        List<FoodOrderRequest> requestedFood = request.foodItems() == null ? List.of() : request.foodItems();
        Map<String, FoodMenuItemDto> menuById = FOOD_MENU.stream()
            .collect(java.util.stream.Collectors.toMap(FoodMenuItemDto::itemId, item -> item));
        List<FoodMenuItemDto> orderedFood = requestedFood.stream()
            .map(item -> {
                FoodMenuItemDto menuItem = menuById.get(item.itemId());
                if (menuItem == null) {
                throw new InvalidRequestException(
                    "One or more food items are not available",
                    Map.of("foodItems", "Choose items from the CineVerse menu"));
                }
                return menuItem;
            })
            .toList();
        BigDecimal foodSubtotal = requestedFood.stream()
            .map(item -> menuById.get(item.itemId()).unitPrice().multiply(BigDecimal.valueOf(item.quantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalPrice = seatSubtotal.add(foodSubtotal).setScale(2, RoundingMode.HALF_UP);
        String reference = "CV-" + UUID.randomUUID().toString().toUpperCase(Locale.ROOT);
        CustomerAccount customer = customerAccountId == null ? null : customerAccountRepository.findById(customerAccountId)
                .orElseThrow(() -> new com.cineverse.exception.ResourceNotFoundException("Customer", customerAccountId));
        String paymentMethod = request.paymentMethod() == null
                ? "DEMO"
                : request.paymentMethod().toUpperCase(Locale.ROOT);
        Booking booking = new Booking(reference, show, totalPrice, seatSubtotal, foodSubtotal, customer, paymentMethod);
        selectedSeats.forEach(booking::addSeat);
        for (int index = 0; index < requestedFood.size(); index++) {
            FoodOrderRequest order = requestedFood.get(index);
            FoodMenuItemDto item = orderedFood.get(index);
            booking.addFoodItem(item.itemId(), item.name(), order.quantity(), item.unitPrice());
        }
        return toDto(bookingRepository.save(booking));
    }

    @Transactional(readOnly = true)
    public BookingDto getBooking(Long bookingId) {
        return getBooking(bookingId, null);
    }

    @Transactional(readOnly = true)
    public BookingDto getBooking(Long bookingId, Long customerAccountId) {
        return toDto(getOwnedBooking(bookingId, customerAccountId));
    }

    @Transactional(readOnly = true)
    public List<BookingDto> getBookingsForCustomer(Long customerAccountId) {
        return bookingRepository.findByCustomerAccount_IdOrderByCreatedAtDesc(customerAccountId)
                .stream()
                .map(BookingService::toDto)
                .toList();
    }

    @Transactional
    public BookingDto cancelBooking(Long bookingId, Long customerAccountId, String reason) {
        Booking booking = getOwnedBooking(bookingId, customerAccountId, true);
        if (booking.getStatus() != com.cineverse.entity.BookingStatus.CONFIRMED) {
            throw new RequestConflictException("This booking has already been cancelled or is no longer eligible");
        }
        if (!booking.getShow().getStartTime().isAfter(LocalDateTime.now())) {
            throw new RequestConflictException("Bookings can only be cancelled before the show starts");
        }
        booking.cancel(reason.trim());
        return toDto(booking);
    }

    @Transactional
    public BookingDto requestAftercare(Long bookingId, Long customerAccountId, BookingAftercareType type) {
        Booking booking = getOwnedBooking(bookingId, customerAccountId, true);
        if (booking.getStatus() != com.cineverse.entity.BookingStatus.CANCELLED) {
            throw new RequestConflictException("Refund or exchange demo requests are available only after cancellation");
        }
        if (booking.getAftercareType() != null) {
            throw new RequestConflictException("A refund or exchange request has already been recorded for this booking");
        }
        booking.requestAftercare(type);
        return toDto(booking);
    }

    @Transactional
    public void removeCancelledBooking(Long bookingId, Long customerAccountId) {
        Booking booking = getOwnedBooking(bookingId, customerAccountId, true);
        if (booking.getStatus() != com.cineverse.entity.BookingStatus.CANCELLED) {
            throw new RequestConflictException("Only cancelled bookings can be removed");
        }
        bookingRepository.delete(booking);
    }

    private Booking getOwnedBooking(Long bookingId, Long customerAccountId) {
        return getOwnedBooking(bookingId, customerAccountId, false);
    }

    private Booking getOwnedBooking(Long bookingId, Long customerAccountId, boolean forUpdate) {
        if (customerAccountId == null) throw new AuthenticationRequiredException();
        Booking booking = (forUpdate
                ? bookingRepository.findByIdForUpdate(bookingId)
                : bookingRepository.findById(bookingId))
                .orElseThrow(() -> new com.cineverse.exception.ResourceNotFoundException("Booking", bookingId));
        Long ownerId = booking.getCustomerAccount() == null ? null : booking.getCustomerAccount().getId();
        if (!customerAccountId.equals(ownerId)) {
            throw new com.cineverse.exception.ResourceNotFoundException("Booking", bookingId);
        }
        return booking;
    }

    private static BookingDto toDto(Booking booking) {
        Show show = booking.getShow();
        List<String> seatLabels = booking.getBookedSeats().stream()
                .sorted(Comparator
                        .comparing((BookingSeat bookedSeat) -> bookedSeat.getSeat().getRowNumber())
                        .thenComparing(bookedSeat -> bookedSeat.getSeat().getSeatNumber()))
                .map(bookedSeat -> bookedSeat.getSeat().getLabel())
                .toList();

        return new BookingDto(
                booking.getId(),
                booking.getBookingReference(),
                booking.getStatus(),
                show.getId(),
                show.getMovie().getTitle(),
                show.getScreen().getCinema().getName(),
                show.getScreen().getCinema().getCity(),
                show.getScreen().getName(),
                show.getStartTime(),
                seatLabels,
                booking.getTotalPrice(),
                booking.getCreatedAt(),
                booking.getCancellationReason(),
                booking.getCancelledAt(),
                booking.getAftercareType(),
                booking.getAftercareStatus(),
                booking.getAftercareRequestedAt(),
                booking.getPaymentMethod() == null ? "DEMO" : booking.getPaymentMethod(),
                "SIMULATED_SUCCESS",
                show.getScreen().getCinema().getAddress(),
                show.getTicketPrice(),
                booking.getSeatSubtotal(),
                booking.getFoodSubtotal(),
                booking.getFoodItems().stream()
                    .map(item -> new FoodOrderDto(
                        item.getItemId(),
                        item.getName(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
                            .setScale(2, RoundingMode.HALF_UP)))
                    .toList());
    }
}
