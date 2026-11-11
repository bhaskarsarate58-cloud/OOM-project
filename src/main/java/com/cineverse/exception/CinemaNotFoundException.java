package com.cineverse.exception;

public class CinemaNotFoundException extends ResourceNotFoundException {

    public CinemaNotFoundException(Long id) {
        super("Cinema", id);
    }
}
