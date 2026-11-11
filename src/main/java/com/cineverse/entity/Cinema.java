package com.cineverse.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "cinemas",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_cinema_name_city",
                columnNames = {"name", "city"}))
public class Cinema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 250)
    private String address;

    @Column(nullable = false, length = 100)
    private String city;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "cinema_facilities", joinColumns = @JoinColumn(name = "cinema_id"))
    @OrderColumn(name = "facility_order")
    @Column(name = "facility", nullable = false, length = 80)
    private List<String> facilities = new ArrayList<>();

    @OneToMany(mappedBy = "cinema")
    private List<Screen> screens = new ArrayList<>();

    protected Cinema() {
        // Required by JPA.
    }

    public Cinema(String name, String address, String city, List<String> facilities) {
        this.name = name;
        this.address = address;
        this.city = city;
        this.facilities = new ArrayList<>(facilities);
    }

    public void updateDetails(String name, String address, String city, List<String> facilities) {
        this.name = name;
        this.address = address;
        this.city = city;
        this.facilities.clear();
        this.facilities.addAll(facilities);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getCity() {
        return city;
    }

    public List<String> getFacilities() {
        return Collections.unmodifiableList(facilities);
    }

}
