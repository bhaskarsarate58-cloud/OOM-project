package com.cineverse.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Service;

@Service
public class CsrfTokenService {

    private static final String CSRF_TOKEN = CsrfTokenService.class.getName() + ".csrfToken";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public String getOrCreate(HttpSession session) {
        Object existing = session.getAttribute(CSRF_TOKEN);
        if (existing instanceof String token) return token;
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        session.setAttribute(CSRF_TOKEN, token);
        return token;
    }

    public boolean isValid(HttpSession session, String suppliedToken) {
        if (session == null || suppliedToken == null) return false;
        Object expectedToken = session.getAttribute(CSRF_TOKEN);
        return expectedToken instanceof String expected
                && MessageDigest.isEqual(
                        expected.getBytes(StandardCharsets.UTF_8),
                        suppliedToken.getBytes(StandardCharsets.UTF_8));
    }
}
