package com.cineverse.service;

import jakarta.servlet.http.HttpSession;

public final class CustomerSession {

    public static final String ACCOUNT_ID_ATTRIBUTE = CustomerSession.class.getName() + ".accountId";

    private CustomerSession() {
    }

    public static Long accountId(HttpSession session) {
        if (session == null) return null;
        Object accountId = session.getAttribute(ACCOUNT_ID_ATTRIBUTE);
        return accountId instanceof Long id ? id : null;
    }
}
