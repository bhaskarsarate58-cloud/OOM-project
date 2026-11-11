package com.cineverse.exception;

public class MovieNotFoundException extends ResourceNotFoundException {

    public MovieNotFoundException(Long id) {
        super("Movie", id);
    }
}
