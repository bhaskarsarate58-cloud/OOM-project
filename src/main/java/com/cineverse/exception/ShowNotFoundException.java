package com.cineverse.exception;

public class ShowNotFoundException extends ResourceNotFoundException {

    public ShowNotFoundException(Long id) {
        super("Show", id);
    }
}
