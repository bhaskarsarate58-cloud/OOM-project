package com.cineverse.exception;

public class AuthenticationRequiredException extends RuntimeException {

    public AuthenticationRequiredException() {
        super("Please log in to continue with your booking");
    }
}
