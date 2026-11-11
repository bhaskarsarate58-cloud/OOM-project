package com.cineverse.exception;

public class ScreenNotFoundException extends ResourceNotFoundException {

    public ScreenNotFoundException(Long id) {
        super("Screen", id);
    }
}
