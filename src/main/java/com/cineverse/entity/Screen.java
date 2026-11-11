package com.cineverse.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "screens",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_screen_cinema_name",
                columnNames = {"cinema_id", "name"}))
public class Screen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(nullable = false)
    private Integer rowCount;

    @Column(nullable = false)
    private Integer seatsPerRow;

    @Column(nullable = false)
    private Integer totalSeatCapacity;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cinema_id", nullable = false)
    private Cinema cinema;

    @OneToMany(mappedBy = "screen", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Show> shows = new ArrayList<>();

    @OneToMany(mappedBy = "screen", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Seat> seats = new ArrayList<>();

    protected Screen() {
        // Required by JPA.
    }

    public Screen(String name, int rowCount, int seatsPerRow, Cinema cinema) {
        this.name = name;
        this.rowCount = rowCount;
        this.seatsPerRow = seatsPerRow;
        this.totalSeatCapacity = rowCount * seatsPerRow;
        this.cinema = cinema;
        for (int row = 1; row <= rowCount; row++) {
            String rowLabel = rowLabel(row);
            for (int seatNumber = 1; seatNumber <= seatsPerRow; seatNumber++) {
                seats.add(new Seat(this, row, rowLabel, seatNumber));
            }
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Integer getRowCount() {
        return rowCount;
    }

    public Integer getSeatsPerRow() {
        return seatsPerRow;
    }

    public Integer getTotalSeatCapacity() {
        return totalSeatCapacity;
    }

    public Cinema getCinema() {
        return cinema;
    }

    private static String rowLabel(int rowNumber) {
        StringBuilder label = new StringBuilder();
        int value = rowNumber;
        while (value > 0) {
            value--;
            label.insert(0, (char) ('A' + value % 26));
            value /= 26;
        }
        return label.toString();
    }
}
