(() => {
  "use strict";

  async function request(path) {
    let response;
    try {
      response = await fetch(path, {
        headers: { Accept: "application/json" },
        credentials: "same-origin"
      });
    } catch (error) {
      throw new Error("We couldn't reach CineVerse. Check your connection and try again.");
    }

    if (!response.ok) {
      let message = "Something went wrong while loading this information.";
      let body = null;
      try {
        body = await response.json();
        if (body && typeof body.message === "string") message = body.message;
      } catch (error) {
        // Some server errors do not include a JSON response body.
      }
      const error = new Error(message);
      error.status = response.status;
      error.fieldErrors = body && body.fieldErrors ? body.fieldErrors : {};
      throw error;
    }
    return response.json();
  }

  async function authRequest(path, method, body) {
    const csrf = await request("/api/auth/csrf");
    let response;
    try {
      response = await fetch(path, {
        method,
        headers: {
          Accept: "application/json",
          "Content-Type": "application/json",
          "X-CSRF-Token": csrf.token
        },
        credentials: "same-origin",
        body: body == null ? undefined : JSON.stringify(body)
      });
    } catch (error) {
      throw new Error("We couldn't reach CineVerse. Check your connection and try again.");
    }
    if (!response.ok) {
      let message = `${method} ${path} failed with HTTP ${response.status}.`;
      let fieldErrors = {};
      try {
        const result = await response.json();
        if (result && typeof result.message === "string") message = result.message;
        if (result && result.fieldErrors) fieldErrors = result.fieldErrors;
      } catch (error) {
        // Preserve a useful message when the server does not return JSON.
      }
      const error = new Error(message);
      error.status = response.status;
      error.fieldErrors = fieldErrors;
      throw error;
    }
    if (response.status === 204) return null;
    return response.json();
  }

  window.CineVerseApi = {
    registerAccount(details) {
      return authRequest("/api/auth/register", "POST", details);
    },
    loginAccount(details) {
      return authRequest("/api/auth/login", "POST", details);
    },
    loginAdmin(details) {
      return authRequest("/api/auth/admin/login", "POST", details);
    },
    getCurrentUser() {
      return request("/api/auth/me");
    },
    getMyBookings() {
      return request("/api/auth/me/bookings");
    },
    cancelBooking(bookingId, reason) {
      return authRequest(`/api/bookings/${encodeURIComponent(bookingId)}/cancel`, "POST", { reason });
    },
    requestBookingAftercare(bookingId, type) {
      return authRequest(`/api/bookings/${encodeURIComponent(bookingId)}/aftercare`, "POST", { type });
    },
    getAdminDashboard(from = "", to = "") {
      const params = new URLSearchParams();
      if (from) params.set("from", from);
      if (to) params.set("to", to);
      const query = params.toString();
      return request(`/api/admin/dashboard${query ? `?${query}` : ""}`);
    },
    getAdminShows() {
      return request("/api/admin/shows");
    },
    updateAdminShowPrice(showId, ticketPrice) {
      return authRequest(`/api/admin/shows/${encodeURIComponent(showId)}/price`, "PATCH", { ticketPrice });
    },
    logoutAccount() {
      return authRequest("/api/auth/logout", "POST");
    },
    getMovies() {
      return request("/api/movies");
    },
    searchMovies(title) {
      return request(`/api/movies/search?title=${encodeURIComponent(title)}`);
    },
    getDiscoveryStatus() {
      return request("/api/discovery/status");
    },
    getDiscoveryMovies(category, page = 1, query = "") {
      const params = new URLSearchParams({
        category: String(category || "all"),
        page: String(Math.max(1, Number(page) || 1))
      });
      if (String(query || "").trim()) params.set("query", String(query).trim());
      return request(`/api/discovery/movies?${params.toString()}`);
    },
    getDiscoveryMovie(id) {
      return request(`/api/discovery/movies/${encodeURIComponent(id)}`);
    },
    getBookingShows(source, movieId, city, date) {
      const path = source === "local" ? "/api/movies/booking-shows" : "/api/discovery/movies/booking-shows";
      return authRequest(path, "POST", { source, movieId: String(movieId), city, date });
    },
    getMovie(id) {
      return request(`/api/movies/${encodeURIComponent(id)}`);
    },
    getCinemas() {
      return request("/api/cinemas");
    },
    getShowsByMovie(movieId) {
      return request(`/api/movies/${encodeURIComponent(movieId)}/shows`);
    },
    getShowsByCinema(cinemaId) {
      return request(`/api/cinemas/${encodeURIComponent(cinemaId)}/shows`);
    },
    getShow(showId) {
      return request(`/api/shows/${encodeURIComponent(showId)}`);
    },
    getSeatsForShow(showId) {
      return request(`/api/shows/${encodeURIComponent(showId)}/seats`);
    },
    getFoodMenu() {
      return request("/api/booking-food-menu");
    },
    createBooking(showId, seatLabels, paymentMethod, foodItems = []) {
      return authRequest("/api/bookings", "POST", {
        showId: Number(showId),
        seatLabels,
        foodItems,
        paymentMethod,
        paymentOutcome: "SUCCESS"
      });
    },
    removeCancelledBooking(bookingId) {
      return authRequest(`/api/bookings/${encodeURIComponent(bookingId)}`, "DELETE");
    },
    getBooking(bookingId) {
      return request(`/api/bookings/${encodeURIComponent(bookingId)}`);
    }
  };
})();
