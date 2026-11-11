package com.cineverse.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "movies")
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false, length = 60)
    private String genre;

    @Column(nullable = false, length = 60)
    private String language;

    @Column(nullable = false)
    private Integer durationMinutes;

    @Column(nullable = false, precision = 3, scale = 1)
    private BigDecimal rating;

    @Column(length = 2048)
    private String posterUrl;

    @Column(nullable = false)
    private LocalDate releaseDate;

    @OneToMany(mappedBy = "movie")
    private List<Show> shows = new ArrayList<>();

    protected Movie() {
        // Required by JPA.
    }

    public Movie(
            String title,
            String description,
            String genre,
            String language,
            Integer durationMinutes,
            BigDecimal rating,
            String posterUrl,
            LocalDate releaseDate) {
        this.title = title;
        this.description = description;
        this.genre = genre;
        this.language = language;
        this.durationMinutes = durationMinutes;
        this.rating = rating;
        this.posterUrl = posterUrl;
        this.releaseDate = releaseDate;
    }

    public void updateDetails(
            String title,
            String description,
            String genre,
            String language,
            Integer durationMinutes,
            BigDecimal rating,
            String posterUrl,
            LocalDate releaseDate) {
        this.title = title;
        this.description = description;
        this.genre = genre;
        this.language = language;
        this.durationMinutes = durationMinutes;
        this.rating = rating;
        this.posterUrl = posterUrl;
        this.releaseDate = releaseDate;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getGenre() {
        return genre;
    }

    public String getLanguage() {
        return language;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public BigDecimal getRating() {
        return rating;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

}
