package com.cineverse.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String bookingReference;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_account_id")
    private CustomerAccount customerAccount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus status;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal seatSubtotal;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal foodSubtotal;

    @Column(length = 20)
    private String paymentMethod;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(length = 500)
    private String cancellationReason;

    private LocalDateTime cancelledAt;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private BookingAftercareType aftercareType;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private BookingAftercareStatus aftercareStatus;

    private LocalDateTime aftercareRequestedAt;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BookingSeat> bookedSeats = new ArrayList<>();

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BookingFoodItem> foodItems = new ArrayList<>();

    protected Booking() {
        // Required by JPA.
    }

    public Booking(String bookingReference, Show show, BigDecimal totalPrice) {
        this(bookingReference, show, totalPrice, totalPrice, BigDecimal.ZERO, null, "DEMO");
    }

    public Booking(String bookingReference, Show show, BigDecimal totalPrice, CustomerAccount customerAccount) {
        this(bookingReference, show, totalPrice, totalPrice, BigDecimal.ZERO, customerAccount, "DEMO");
    }

    public Booking(
            String bookingReference,
            Show show,
            BigDecimal totalPrice,
            CustomerAccount customerAccount,
            String paymentMethod) {
        this(bookingReference, show, totalPrice, totalPrice, BigDecimal.ZERO, customerAccount, paymentMethod);
    }

    public Booking(
            String bookingReference,
            Show show,
            BigDecimal totalPrice,
            BigDecimal seatSubtotal,
            BigDecimal foodSubtotal,
            CustomerAccount customerAccount,
            String paymentMethod) {
        this.bookingReference = bookingReference;
        this.show = show;
        this.totalPrice = totalPrice;
        this.seatSubtotal = seatSubtotal;
        this.foodSubtotal = foodSubtotal;
        this.customerAccount = customerAccount;
        this.paymentMethod = paymentMethod;
        this.status = BookingStatus.CONFIRMED;
        this.createdAt = LocalDateTime.now();
    }

    public void addSeat(Seat seat) {
        bookedSeats.add(new BookingSeat(this, show, seat));
    }

    public void addFoodItem(String itemId, String name, int quantity, BigDecimal unitPrice) {
        foodItems.add(new BookingFoodItem(this, itemId, name, quantity, unitPrice));
    }

    public void cancel(String reason) {
        this.status = BookingStatus.CANCELLED;
        this.cancellationReason = reason;
        this.cancelledAt = LocalDateTime.now();
    }

    public void requestAftercare(BookingAftercareType type) {
        this.aftercareType = type;
        this.aftercareStatus = BookingAftercareStatus.PENDING;
        this.aftercareRequestedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getBookingReference() {
        return bookingReference;
    }

    public Show getShow() {
        return show;
    }

    public CustomerAccount getCustomerAccount() {
        return customerAccount;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public BigDecimal getSeatSubtotal() {
        return seatSubtotal;
    }

    public BigDecimal getFoodSubtotal() {
        return foodSubtotal;
    }

    public List<BookingFoodItem> getFoodItems() {
        return List.copyOf(foodItems);
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public BookingAftercareType getAftercareType() {
        return aftercareType;
    }

    public BookingAftercareStatus getAftercareStatus() {
        return aftercareStatus;
    }

    public LocalDateTime getAftercareRequestedAt() {
        return aftercareRequestedAt;
    }

    public List<BookingSeat> getBookedSeats() {
        return List.copyOf(bookedSeats);
    }
}
