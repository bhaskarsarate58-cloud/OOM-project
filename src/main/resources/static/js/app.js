(() => {
  "use strict";

  const api = window.CineVerseApi;
  const app = document.getElementById("app");
  const state = {
    movies: [],
    catalogMovies: [],
    catalogGenres: [],
    catalogQuery: "",
    catalogGenre: "",
    catalogLanguages: [],
    catalogLanguage: "",
    catalogSort: "asc",
    catalogRemoteMovies: [],
    catalogRemoteMessage: "",
    catalogRemoteLoading: false,
    catalogSearchSequence: 0,
    catalogSearchTimer: null,
    viewSequence: 0,
    adminReportRequest: 0,
    spotlightMovies: [],
    spotlightIndex: 0,
    spotlightLabel: "FEATURED",
    spotlightTimer: null,
    shownRecords: new Map(),
    selectedSeatLabels: new Set(),
    currentBookingShow: null,
    seatRowsByLabel: new Map(),
    foodMenu: [],
    foodQuantities: new Map(),
    bookingNotice: "",
    checkoutMethod: "upi",
    checkoutSubmitting: false,
    ticketCount: 1,
    bookingDate: "",
    bookingSource: "",
    bookingMovieId: "",
    bookingEntries: [],
    showSchedule: [],
    showScheduleCinemas: [],
    showDate: "all",
    discoveryStatus: null,
    discoveryCategory: "all",
    discoveryQuery: "",
    discoveryChip: "all",
    discoveryPage: 1,
    discoveryTotalPages: 1,
    discoveryResults: [],
    discoveryLoading: false,
    authUser: null,
    authReady: false,
    authError: "",
    customerBookings: [],
    bookingDateFilter: "",
    bookingStatusFilter: "",
    cinemas: [],
    selectedCity: localStorage.getItem("cineverse-city") || "",
    theme: localStorage.getItem("cineverse-theme") === "light" ? "light" : "dark"
  };

  const discoveryCategories = [
    { slug: "trending", label: "Trending Now" },
    { slug: "popular", label: "Popular" },
    { slug: "bollywood", label: "Bollywood" },
    { slug: "hollywood", label: "Hollywood" },
    { slug: "south-indian", label: "South Indian" },
    { slug: "korean", label: "Korean" },
    { slug: "japanese", label: "Japanese" },
    { slug: "upcoming", label: "Upcoming" },
    { slug: "top-rated", label: "Top Rated" },
    { slug: "family-animation", label: "Family & Animation" },
    { slug: "action-adventure", label: "Action & Adventure" },
    { slug: "horror-thriller", label: "Horror & Thriller" },
    { slug: "romance-drama", label: "Romance & Drama" },
    { slug: "sci-fi-fantasy", label: "Sci-Fi & Fantasy" }
  ];
  const discoveryChips = [
    { slug: "all", label: "All Movies" },
    { slug: "trending", label: "Trending" },
    { slug: "bollywood", label: "Bollywood" },
    { slug: "hollywood", label: "Hollywood" },
    { slug: "south-indian", label: "South Indian" },
    { slug: "korean", label: "Korean" },
    { slug: "japanese", label: "Japanese" },
    { slug: "action", label: "Action" },
    { slug: "comedy", label: "Comedy" },
    { slug: "romance", label: "Romance" },
    { slug: "horror", label: "Horror" },
    { slug: "thriller", label: "Thriller" },
    { slug: "animation", label: "Animation" },
    { slug: "sci-fi", label: "Sci-Fi" },
    { slug: "upcoming", label: "Upcoming" }
  ];
  const homeDiscoveryCategories = [
    "trending", "top-rated", "bollywood", "hollywood", "south-indian", "korean", "japanese",
    "action-adventure", "horror-thriller", "romance-drama", "sci-fi-fantasy", "family-animation", "upcoming"
  ];
  const indianCities = [
    "Ahmedabad", "Bengaluru", "Bhopal", "Bhubaneswar", "Chandigarh", "Chennai", "Coimbatore",
    "Dehradun", "Delhi", "Goa", "Guwahati", "Hyderabad", "Indore", "Jaipur", "Kanpur",
    "Kochi", "Kolkata", "Lucknow", "Mumbai", "Nagpur", "Patna", "Prayagraj", "Pune",
    "Ranchi", "Surat", "Varanasi", "Visakhapatnam"
  ];
  const catalogGenreChoices = [
    "Action", "Adventure", "Animation", "Comedy", "Crime", "Documentary", "Drama", "Family",
    "Fantasy", "History", "Horror", "Music", "Musical", "Mystery", "Romance", "Science Fiction",
    "Thriller", "War", "Western", "TV Movie"
  ];
  const catalogLanguageChoices = [
    "Arabic", "Assamese", "Bengali", "Bhojpuri", "Chinese", "Czech", "Danish", "Dutch", "English",
    "French", "German", "Greek", "Gujarati", "Hebrew", "Hindi", "Indonesian", "Italian", "Japanese",
    "Kannada", "Korean", "Malayalam", "Marathi", "Nepali", "Norwegian", "Persian", "Polish",
    "Punjabi", "Portuguese", "Russian", "Sinhala", "Spanish", "Swedish", "Tamil", "Telugu", "Thai",
    "Turkish", "Ukrainian", "Urdu", "Vietnamese", "Other"
  ];
  const indianFallbackMovies = [
    {
      title: "3 Idiots",
      posterUrl: "https://image.tmdb.org/t/p/w500/66A9MqXOyVFCssoloscw79z8Tew.jpg",
      genre: "Comedy · Drama",
      language: "Hindi",
      releaseDate: "2009-12-25",
      bookingKey: "three-idiots"
    },
    {
      title: "RRR",
      posterUrl: "https://image.tmdb.org/t/p/w500/wE0I6efAW4cDDmZQWtwZMOW44EJ.jpg",
      genre: "Action · Drama",
      language: "Telugu",
      releaseDate: "2022-03-25",
      bookingKey: "rrr"
    }
  ];
  const internationalFallbackMovies = [
    {
      title: "Inception",
      posterUrl: "https://image.tmdb.org/t/p/w500/oYuLEt3zVCKq57qu2F8dT7NIa6f.jpg",
      genre: "Science Fiction",
      releaseDate: "2010-07-16",
      bookingKey: "inception"
    },
    {
      title: "Interstellar",
      posterUrl: "https://image.tmdb.org/t/p/w500/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg",
      genre: "Science Fiction",
      releaseDate: "2014-11-07",
      bookingKey: "interstellar"
    },
    {
      title: "The Dark Knight",
      posterUrl: "https://image.tmdb.org/t/p/w500/qJ2tW6WMUDux911r6m7haRef0WH.jpg",
      genre: "Action",
      releaseDate: "2008-07-18",
      bookingKey: "dark-knight"
    }
  ];

  const icons = {
    arrow: '<svg viewBox="0 0 20 20" fill="none" aria-hidden="true"><path d="M3.5 10h12m-5-5 5 5-5 5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>',
    play: '<svg viewBox="0 0 20 20" fill="none" aria-hidden="true"><path d="M6.2 4.6v10.8L15.5 10 6.2 4.6Z" fill="currentColor"/></svg>',
    star: '<svg viewBox="0 0 16 16" fill="none" aria-hidden="true"><path d="m8 1.4 2 4.1 4.5.7-3.3 3.2.8 4.6L8 11.8l-4 2.2.8-4.6-3.3-3.2L6 5.5l2-4.1Z" fill="currentColor"/></svg>',
    search: '<svg viewBox="0 0 20 20" fill="none" aria-hidden="true"><circle cx="8.8" cy="8.8" r="5.8" stroke="currentColor" stroke-width="1.6"/><path d="m13.2 13.2 4 4" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/></svg>',
    alert: '<svg viewBox="0 0 20 20" fill="none" aria-hidden="true"><path d="M10 2.2 18 16H2L10 2.2Z" stroke="currentColor" stroke-width="1.4" stroke-linejoin="round"/><path d="M10 7v4.2m0 2.4v.1" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/></svg>',
    ticket: '<svg viewBox="0 0 24 24" fill="none" aria-hidden="true"><path d="M4 7.5A2.5 2.5 0 0 0 6.5 5h11A2.5 2.5 0 0 0 20 7.5v2a2.5 2.5 0 0 0 0 5v2a2.5 2.5 0 0 0-2.5 2.5h-11A2.5 2.5 0 0 0 4 16.5v-2a2.5 2.5 0 0 0 0-5v-2Z" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/><path d="M12 7.5v1m0 2v1m0 2v1" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/></svg>',
    shield: '<svg viewBox="0 0 20 20" fill="none" aria-hidden="true"><path d="M10 2 16 4v4.7c0 3.8-2.5 6.5-6 8.3-3.5-1.8-6-4.5-6-8.3V4l6-2Z" stroke="currentColor" stroke-width="1.4" stroke-linejoin="round"/><path d="m7.4 9.7 1.8 1.8 3.7-3.8" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round"/></svg>'
  };

  function escapeHtml(value) {
    return String(value == null ? "" : value).replace(/[&<>"']/g, (character) => ({
      "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
    }[character]));
  }

  function posterUrl(value) {
    const url = String(value || "").trim();
    if (/^https?:\/\//i.test(url)) return url;
    if (/^\/(?!\/)/.test(url)) return url;
    return "";
  }

  function ratingText(value) {
    const rating = Number(value);
    return Number.isFinite(rating) ? rating.toFixed(1) : "—";
  }

  function movieYear(movie) {
    if (!movie || !movie.releaseDate) return "";
    const year = String(movie.releaseDate).slice(0, 4);
    return /^\d{4}$/.test(year) ? year : "";
  }

  function durationText(minutes) {
    const total = Number(minutes);
    if (!Number.isFinite(total) || total <= 0) return "";
    const hours = Math.floor(total / 60);
    const remainder = total % 60;
    return `${hours ? `${hours}h ` : ""}${remainder ? `${remainder}m` : ""}`.trim();
  }

  function formatTime(value) {
    if (!value) return "Time unavailable";
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return "Time unavailable";
    return new Intl.DateTimeFormat(undefined, { hour: "numeric", minute: "2-digit" }).format(date);
  }

  function formatShowDate(value) {
    if (!value) return "";
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return "";
    return new Intl.DateTimeFormat(undefined, { weekday: "short", month: "short", day: "numeric" }).format(date);
  }

  function localDateValue(date) {
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}-${String(date.getDate()).padStart(2, "0")}`;
  }

  const languageNames = {
    ar: "Arabic", as: "Assamese", bn: "Bengali", bho: "Bhojpuri", cn: "Chinese", cs: "Czech",
    da: "Danish", de: "German", el: "Greek", en: "English", es: "Spanish", fa: "Persian",
    fr: "French", gu: "Gujarati", he: "Hebrew", hi: "Hindi",
    id: "Indonesian", it: "Italian", ja: "Japanese", kn: "Kannada", ko: "Korean",
    ml: "Malayalam", mr: "Marathi", ne: "Nepali", no: "Norwegian", pa: "Punjabi", pl: "Polish",
    pt: "Portuguese", ru: "Russian", si: "Sinhala", sv: "Swedish", ta: "Tamil", te: "Telugu",
    th: "Thai", tr: "Turkish", uk: "Ukrainian", ur: "Urdu", vi: "Vietnamese", zh: "Chinese"
  };

  function languageLabel(value) {
    const language = String(value || "").trim();
    if (!language) return "";
    return languageNames[language.toLowerCase()] || language;
  }

  function applyTheme(theme) {
    state.theme = theme === "light" ? "light" : "dark";
    document.documentElement.dataset.theme = state.theme;
    const button = document.getElementById("theme-toggle");
    if (button) {
      const nextTheme = state.theme === "dark" ? "light" : "dark";
      button.setAttribute("aria-label", `Switch to ${nextTheme} theme`);
      button.title = `Switch to ${nextTheme} theme`;
      button.innerHTML = `<span aria-hidden="true">${state.theme === "dark" ? "☼" : "◐"}</span>`;
    }
    document.querySelector('meta[name="theme-color"]')?.setAttribute("content", state.theme === "dark" ? "#111112" : "#f2f0eb");
  }

  function renderCityOptions(cinemas) {
    state.cinemas = Array.isArray(cinemas) ? cinemas : [];
    const cities = [...new Set([...indianCities, ...state.cinemas.map((cinema) => String(cinema.city || "").trim()).filter(Boolean)])]
      .sort((left, right) => left.localeCompare(right));
    if (state.selectedCity) {
      state.selectedCity = cities.find((city) => city.toLocaleLowerCase() === state.selectedCity.toLocaleLowerCase()) || "";
    }
    const selector = document.getElementById("city-selector");
    if (selector) {
      selector.innerHTML = `<option value="">All cities</option>${cities.map((city) =>
        `<option value="${escapeHtml(city)}">${escapeHtml(city)}</option>`
      ).join("")}`;
      selector.value = state.selectedCity;
    }
  }

  async function loadCityOptions() {
    try {
      renderCityOptions(await api.getCinemas());
    } catch (error) {
      const selector = document.getElementById("city-selector");
      if (selector) selector.title = `City list unavailable: ${error.message}`;
    }
  }

  function priceText(value) {
    if (value == null || value === "") return "";
    const amount = Number(value);
    return Number.isFinite(amount) ? `Listed price ${amount.toFixed(2)}` : "";
  }

  function setActiveNavigation(routeName) {
    document.querySelectorAll(".nav-link").forEach((link) => {
      const isActive = link.dataset.route === routeName;
      link.classList.toggle("active", isActive);
      if (isActive) link.setAttribute("aria-current", "page");
      else link.removeAttribute("aria-current");
    });
  }

  function renderAuthNavigation() {
    const target = document.getElementById("auth-nav");
    if (!target) return;
    if (state.authUser) {
      target.innerHTML = `<a class="auth-nav-profile" href="#/profile">${escapeHtml(state.authUser.displayName)}</a><button class="auth-nav-logout" type="button" data-logout>Log out</button>`;
    } else {
      target.innerHTML = `<a class="auth-nav-login" href="#/login">Log in</a><a class="auth-nav-signup" href="#/signup">Sign up</a>`;
    }
    target.title = state.authError;
  }

  async function logoutAccount(button) {
    button.disabled = true;
    try {
      await api.logoutAccount();
      state.authUser = null;
      state.authReady = true;
      state.authError = "";
      state.customerBookings = [];
      renderAuthNavigation();
      location.hash = "#/";
    } catch (error) {
      state.authError = error.message;
      renderAuthNavigation();
    }
  }

  document.getElementById("auth-nav").addEventListener("click", (event) => {
    const button = event.target.closest("[data-logout]");
    if (button) logoutAccount(button);
  });

  document.getElementById("theme-toggle").addEventListener("click", () => {
    const theme = state.theme === "dark" ? "light" : "dark";
    localStorage.setItem("cineverse-theme", theme);
    applyTheme(theme);
  });

  document.getElementById("city-selector").addEventListener("change", (event) => {
    state.selectedCity = event.target.value;
    if (state.selectedCity) localStorage.setItem("cineverse-city", state.selectedCity);
    else localStorage.removeItem("cineverse-city");
    const route = parseRoute();
    if (["shows", "bookingStart"].includes(route.name)) routeTo(route);
  });

  function loadingCards(count = 5) {
    return `<div class="loading-grid" aria-label="Loading movies">${Array.from({ length: count }, () => `
      <div class="skeleton-card" aria-hidden="true"><div class="skeleton-poster"></div><div class="skeleton-line"></div><div class="skeleton-line short"></div></div>
    `).join("")}</div>`;
  }

  function statePanel(title, message, retryAction, compact = false) {
    const retry = retryAction
      ? `<button class="button button-secondary" type="button" data-retry="${escapeHtml(retryAction)}">Try again</button>`
      : "";
    return `<div class="state" role="status"><div class="state-inner">
      <span class="state-icon">${icons.alert}</span>
      <h2>${escapeHtml(title)}</h2>
      <p>${escapeHtml(message)}</p>${retry}
    </div></div>`;
  }

  function emptyPanel(title, message) {
    return `<div class="state" role="status"><div class="state-inner">
      <span class="state-icon">${icons.search}</span>
      <h2>${escapeHtml(title)}</h2>
      <p>${escapeHtml(message)}</p>
    </div></div>`;
  }

  function movieCard(movie, index) {
    const image = posterUrl(movie.posterUrl);
    const title = escapeHtml(movie.title || "Untitled film");
    const genre = escapeHtml(movie.genre || "Film");
    const year = movieYear(movie);
    const fallbackText = escapeHtml(movie.title || "CineVerse");
    return `<article class="movie-card" style="animation-delay:${Math.min(index, 8) * 45}ms">
      <div class="poster-shell">
        <a href="#/movies/${encodeURIComponent(movie.id)}" aria-label="View details for ${title}">
          ${image ? `<img class="poster-image" src="${escapeHtml(image)}" alt="${title} poster" loading="lazy">` : ""}
          <span class="poster-fallback" ${image ? "hidden" : ""} aria-hidden="true">${fallbackText}</span>
        </a>
        <span class="card-rating">${icons.star}${escapeHtml(ratingText(movie.rating))}</span>
        <a class="card-overlay button button-primary" href="#/book/local/${encodeURIComponent(movie.id)}">Book Tickets ${icons.arrow}</a>
      </div>
      <h3><a href="#/movies/${encodeURIComponent(movie.id)}">${title}</a></h3>
      <div class="card-subtitle">${genre}${year ? ` <span aria-hidden="true">·</span> ${escapeHtml(year)}` : ""}</div>
    </article>`;
  }

  function discoveryGenres(movie) {
    if (Array.isArray(movie.genres)) return movie.genres.filter(Boolean).join(" · ");
    return movie.genre || "";
  }

  function discoveryRoute(movie) {
    return movie.source === "LOCAL" && movie.localMovieId != null
      ? `#/movies/${encodeURIComponent(movie.localMovieId)}`
      : `#/discover/${encodeURIComponent(movie.id)}`;
  }

  function discoveryBookingRoute(movie) {
    return movie.source === "LOCAL" && movie.localMovieId != null
      ? `#/book/local/${encodeURIComponent(movie.localMovieId)}`
      : `#/book/tmdb/${encodeURIComponent(movie.id)}`;
  }

  function discoveryCard(movie, index = 0) {
    const image = posterUrl(movie.posterUrl);
    const title = escapeHtml(movie.title || "Untitled film");
    const genres = escapeHtml(discoveryGenres(movie) || "Film");
    const year = movieYear(movie);
    const href = discoveryRoute(movie);
    const hasRating = movie.rating != null && Number.isFinite(Number(movie.rating));
    return `<article class="movie-card discovery-card" style="animation-delay:${Math.min(index, 8) * 35}ms">
      <div class="poster-shell">
        <a href="${href}" aria-label="View details for ${title}">
          ${image ? `<img class="poster-image" src="${escapeHtml(image)}" alt="${title} poster" loading="lazy">` : ""}
          <span class="poster-fallback" ${image ? "hidden" : ""} aria-hidden="true">${title}</span>
        </a>
        ${hasRating ? `<span class="card-rating">${icons.star}${escapeHtml(ratingText(movie.rating))}</span>` : ""}
        <a class="card-overlay button button-primary" href="${discoveryBookingRoute(movie)}">Book Tickets ${icons.arrow}</a>
      </div>
      <h3><a href="${href}">${title}</a></h3>
      <div class="card-subtitle">${genres}${year ? ` <span aria-hidden="true">·</span> ${escapeHtml(year)}` : ""}</div>
    </article>`;
  }

  function discoveryStatusMarkup(status) {
    if (status?.liveAvailable) {
      return `<div class="discovery-status is-live" role="status"><span class="status-marker" aria-hidden="true"></span>
        <div><strong>Online discovery is connected</strong><p>${escapeHtml(status.message || "Explore live movie information. Showtimes appear only for films linked to CineVerse cinemas.")}</p></div>
        <span class="status-region">${escapeHtml(status.region || "Live catalog")}</span>
      </div>`;
    }
    const message = status?.message || "The online movie catalog is not configured. This page shows CineVerse's local catalog and scheduled cinema screenings.";
    return `<div class="discovery-status" role="status"><span class="status-marker" aria-hidden="true"></span>
      <div><strong>Local cinema catalog</strong><p>${escapeHtml(message)} Online discovery rankings and national categories are unavailable; local films and their real listed showtimes remain available.</p></div>
      <a class="status-action" href="#/movies">Browse local films ${icons.arrow}</a>
    </div>`;
  }

  function railSkeleton(slug) {
    return `<div class="discovery-rail-loading" data-rail-content="${escapeHtml(slug)}" aria-label="Loading ${escapeHtml(slug)}">
      ${Array.from({ length: 5 }, () => `<div class="rail-skeleton"><div></div><span></span><i></i></div>`).join("")}
    </div>`;
  }

  function discoveryRailMarkup(category) {
    return `<section class="discovery-rail-section" aria-labelledby="rail-${escapeHtml(category.slug)}">
      <div class="section-heading rail-heading">
        <div><span class="eyebrow">Discover</span><h2 id="rail-${escapeHtml(category.slug)}">${escapeHtml(category.label)}</h2></div>
        <div class="rail-tools">
        <a class="text-link" href="#/discover?category=${encodeURIComponent(category.slug)}">View all ${icons.arrow}</a>
          <button class="rail-arrow" type="button" data-scroll-rail="${escapeHtml(category.slug)}" data-direction="-1" aria-label="Scroll ${escapeHtml(category.label)} left">‹</button>
          <button class="rail-arrow" type="button" data-scroll-rail="${escapeHtml(category.slug)}" data-direction="1" aria-label="Scroll ${escapeHtml(category.label)} right">›</button>
        </div>
      </div>
      ${railSkeleton(category.slug)}
    </section>`;
  }

  function renderDiscoveryRail(slug, response, error = "", local = false) {
    const target = app.querySelector(`[data-rail-content="${CSS.escape(slug)}"]`);
    if (!target) return;
    if (error) {
      target.outerHTML = `<div class="rail-state" data-rail-content="${escapeHtml(slug)}">${statePanel("This collection couldn't load", error, `discovery-rail:${slug}`, true)}</div>`;
      return;
    }
    const movies = Array.isArray(response?.results) ? response.results : [];
    target.outerHTML = movies.length
      ? `<div class="discovery-rail" data-rail-content="${escapeHtml(slug)}" tabindex="0" aria-label="${escapeHtml(slug)} movies">${movies.map(local ? movieCard : discoveryCard).join("")}</div>`
      : `<div class="rail-state" data-rail-content="${escapeHtml(slug)}">${emptyPanel("No titles in this collection", "There are no matching titles in this catalog collection.")}</div>`;
    bindPosterFallbacks(app);
  }

  async function loadDiscoveryRail(category, viewId) {
    try {
      const response = await api.getDiscoveryMovies(category.slug, 1, "");
      if (viewId !== state.viewSequence) return;
      renderDiscoveryRail(category.slug, response);
    } catch (error) {
      if (viewId !== state.viewSequence) return;
      renderDiscoveryRail(category.slug, null, error.message);
    }
  }

  function featuredFallbackCard(movie) {
    const image = posterUrl(movie.posterUrl);
    const title = escapeHtml(movie.title || "Untitled film");
    const year = movieYear(movie);
    const href = movie.bookingKey
      ? `#/book/curated/${encodeURIComponent(movie.bookingKey)}`
      : `#/movies?search=${encodeURIComponent(movie.title || "")}`;
    return `<article class="movie-card featured-fallback-card">
      <div class="poster-shell">
        <a href="${href}" aria-label="Book demo tickets for ${title}">
          ${image ? `<img class="poster-image" src="${escapeHtml(image)}" alt="${title} poster" loading="eager">` : ""}
          <span class="poster-fallback" ${image ? "hidden" : ""} aria-hidden="true">${title}</span>
        </a>
      </div>
      <h3><a href="${href}">${title}</a></h3>
      <div class="card-subtitle">${escapeHtml(movie.genre || "Film")}${year ? ` <span aria-hidden="true">·</span> ${escapeHtml(year)}` : ""}</div>
      <div class="card-subtitle">Availability is not configured</div>
      <a class="button button-secondary" href="${href}">${movie.bookingKey ? "Book demo tickets" : "Search movie availability"} ${icons.arrow}</a>
    </article>`;
  }

  function featuredHomeSection(slug, title, movies, options = {}) {
    const isLoading = options.loading === true;
    const content = isLoading
      ? railSkeleton(slug)
      : `<div class="discovery-rail" data-rail-content="${escapeHtml(slug)}" tabindex="0" aria-label="${escapeHtml(title)}">
          ${movies.length
            ? movies.map((movie) => options.online ? discoveryCard(movie)
              : options.mixed && movie.id != null ? movieCard(movie)
                : options.fallback || options.mixed ? featuredFallbackCard(movie) : movieCard(movie)).join("")
            : `<div class="rail-empty">${emptyPanel("No movies available", "Movie listings could not be loaded. Please try again shortly.")}</div>`}
        </div>`;
    return `<section class="discovery-rail-section" aria-labelledby="rail-${escapeHtml(slug)}">
      <div class="section-heading rail-heading">
        <div><span class="eyebrow">${options.online ? "Live discovery" : options.fallback ? "Curated fallback" : "Local catalog"}</span><h2 id="rail-${escapeHtml(slug)}">${escapeHtml(title)}</h2></div>
        <div class="rail-tools">
          <a class="text-link" href="#/movies">View all ${icons.arrow}</a>
          <button class="rail-arrow" type="button" data-scroll-rail="${escapeHtml(slug)}" data-direction="-1" aria-label="Scroll ${escapeHtml(title)} left">‹</button>
          <button class="rail-arrow" type="button" data-scroll-rail="${escapeHtml(slug)}" data-direction="1" aria-label="Scroll ${escapeHtml(title)} right">›</button>
        </div>
      </div>
      <div data-home-featured="${escapeHtml(slug)}">${content}</div>
    </section>`;
  }

  function featuredLocalRailsMarkup(movies, error = "") {
    const isIndian = (movie) => /^(hi|bn|gu|kn|ml|mr|pa|ta|te|ur|hindi|bengali|gujarati|kannada|malayalam|marathi|punjabi|tamil|telugu|urdu)$/i
      .test(String(movie.language || "").trim());
    const indianMovies = movies.filter(isIndian);
    const internationalMovies = movies.filter((movie) => !isIndian(movie));
    const indianTitles = new Set(indianMovies.map((movie) => String(movie.title || "").toLocaleLowerCase()));
    const indianFallback = indianFallbackMovies.filter((movie) => !indianTitles.has(movie.title.toLocaleLowerCase()));
    const internationalTitles = new Set(internationalMovies.map((movie) => String(movie.title || "").toLocaleLowerCase()));
    const internationalFallback = internationalFallbackMovies.filter((movie) => !internationalTitles.has(movie.title.toLocaleLowerCase()));
    return `${featuredHomeSection("indian-movies", "Indian Movies", indianMovies.concat(indianFallback), {
      mixed: true
    })}
      ${featuredHomeSection("international-movies", "International Movies", internationalMovies.concat(
        internationalMovies.length ? [] : internationalFallbackMovies
      ), { mixed: true })}
      ${error ? `<p class="online-fallback" role="status">The local catalog could not be loaded. Curated sample films are shown where available. ${escapeHtml(error)}</p>` : ""}`;
  }

  function replaceFeaturedHomeSection(slug, title, movies, options = {}) {
    const target = app.querySelector(`[data-home-featured="${CSS.escape(slug)}"]`);
    if (!target) return;
    const cards = movies.length
      ? movies.map((movie) => options.online ? discoveryCard(movie)
        : options.mixed && movie.id != null ? movieCard(movie)
          : options.fallback || options.mixed ? featuredFallbackCard(movie) : movieCard(movie)).join("")
      : `<div class="rail-empty">${emptyPanel("No movies available", "Movie listings could not be loaded. Please try again shortly.")}</div>`;
    target.innerHTML = `<div class="discovery-rail" data-rail-content="${escapeHtml(slug)}" tabindex="0" aria-label="${escapeHtml(title)}">
      ${cards}
    </div>`;
    bindPosterFallbacks(target);
  }

  async function loadHomeFeaturedSections(movies, viewId) {
    const [indianResult, internationalResult, trendingResult] = await Promise.all([
      Promise.all([
          api.getDiscoveryMovies("bollywood", 1, ""),
          api.getDiscoveryMovies("south-indian", 1, "")
      ]).then((responses) => ({ responses })).catch((error) => ({ error })),
      api.getDiscoveryMovies("all", 1, "")
        .then((response) => ({ response }))
        .catch((error) => ({ error })),
      api.getDiscoveryMovies("trending", 1, "")
        .then((response) => ({ response }))
        .catch((error) => ({ error }))
    ]);
    if (viewId !== state.viewSequence) return;
    const trendingMovies = trendingResult.response?.results || [];
    setSpotlightMovies(trendingMovies.length ? trendingMovies : movies, viewId,
      trendingMovies.length ? "TRENDING" : "FEATURED");
    const indian = [...new Map((indianResult.responses || []).flatMap((response) => response.results || [])
        .map((movie) => [String(movie.id), movie])).values()];
      const indianLanguageCodes = new Set(["hi", "bn", "gu", "kn", "ml", "mr", "pa", "ta", "te", "ur"]);
      const international = (internationalResult.response?.results || [])
        .filter((movie) => !indianLanguageCodes.has(String(movie.language || "").toLowerCase()));
      replaceFeaturedHomeSection("indian-movies", "Indian Movies", indian.length ? indian : featuredIndianFallback(movies), { online: indian.length > 0, mixed: indian.length === 0 });
      replaceFeaturedHomeSection("international-movies", "International Movies", international.length ? international : featuredInternationalFallback(movies), {
        online: international.length > 0,
        mixed: international.length === 0
      });
    const errors = [indianResult.error, internationalResult.error, trendingResult.error].filter(Boolean);
    const message = document.getElementById("home-featured-error");
    if (errors.length && message) {
      message.textContent = `Some live collections could not load (${errors.map((error) => error.message).join("; ")}). Showing available local and curated movies for those sections.`;
      message.hidden = false;
    }
  }

  function featuredIndianFallback(movies) {
    const local = movies.filter((movie) => /^(hi|bn|gu|kn|ml|mr|pa|ta|te|ur|hindi|bengali|gujarati|kannada|malayalam|marathi|punjabi|tamil|telugu|urdu)$/i
      .test(String(movie.language || "").trim()));
    const titles = new Set(local.map((movie) => String(movie.title || "").toLocaleLowerCase()));
    return local.concat(indianFallbackMovies.filter((movie) => !titles.has(movie.title.toLocaleLowerCase())));
  }

  function featuredInternationalFallback(movies) {
    const local = movies.filter((movie) => !/^(hi|bn|gu|kn|ml|mr|pa|ta|te|ur|hindi|bengali|gujarati|kannada|malayalam|marathi|punjabi|tamil|telugu|urdu)$/i
      .test(String(movie.language || "").trim()));
    return local.length ? local : internationalFallbackMovies;
  }

  function localDiscoveryRailsMarkup(movies) {
    const languageIs = (movie, pattern) => pattern.test(String(movie.language || ""));
    const genreIs = (movie, pattern) => pattern.test(String(movie.genre || ""));
    const categories = [
      { slug: "trending", label: "Trending Now", movies },
      { slug: "top-rated", label: "Top Rated", movies: [...movies].sort((left, right) => Number(right.rating) - Number(left.rating)) },
      { slug: "bollywood", label: "Bollywood", movies: movies.filter((movie) => languageIs(movie, /hindi/i) || genreIs(movie, /bollywood/i)) },
      { slug: "hollywood", label: "Hollywood", movies: movies.filter((movie) => languageIs(movie, /english/i)) },
      { slug: "south-indian", label: "South Indian", movies: movies.filter((movie) => languageIs(movie, /tamil|telugu|malayalam|kannada/i)) },
      { slug: "korean-japanese", label: "Korean & Japanese", movies: movies.filter((movie) => languageIs(movie, /korean|japanese/i)) },
      { slug: "action-adventure", label: "Action & Adventure", movies: movies.filter((movie) => genreIs(movie, /action|adventure/i)) },
      { slug: "comedy", label: "Comedy", movies: movies.filter((movie) => genreIs(movie, /comedy/i)) },
      { slug: "romance-drama", label: "Romance", movies: movies.filter((movie) => genreIs(movie, /romance/i)) },
      { slug: "horror-thriller", label: "Horror", movies: movies.filter((movie) => genreIs(movie, /horror/i)) },
      { slug: "sci-fi-fantasy", label: "Sci-Fi", movies: movies.filter((movie) => genreIs(movie, /science fiction|sci-fi|fantasy/i)) },
      { slug: "family-animation", label: "Animation", movies: movies.filter((movie) => genreIs(movie, /animation/i)) },
      {
        slug: "upcoming",
        label: "Coming Soon",
        movies: movies.filter((movie) => movie.releaseDate && new Date(`${movie.releaseDate}T00:00:00`) > new Date())
      }
    ];
    return categories.map((category) => `<section class="discovery-rail-section" aria-labelledby="rail-${escapeHtml(category.slug)}">
      <div class="section-heading rail-heading">
        <div><span class="eyebrow">Local catalog</span><h2 id="rail-${escapeHtml(category.slug)}">${escapeHtml(category.label)}</h2></div>
        <div class="rail-tools">
          <a class="text-link" href="#/movies">View all ${icons.arrow}</a>
          <button class="rail-arrow" type="button" data-scroll-rail="${escapeHtml(category.slug)}" data-direction="-1" aria-label="Scroll ${escapeHtml(category.label)} left">‹</button>
          <button class="rail-arrow" type="button" data-scroll-rail="${escapeHtml(category.slug)}" data-direction="1" aria-label="Scroll ${escapeHtml(category.label)} right">›</button>
        </div>
      </div>
      <div class="discovery-rail" data-rail-content="${escapeHtml(category.slug)}" tabindex="0" aria-label="${escapeHtml(category.label)} movies">
        ${category.movies.length
          ? category.movies.map(movieCard).join("")
          : `<div class="rail-empty">${emptyPanel("No matching local titles yet", "This category will fill when matching movies are added to the local catalog.")}</div>`}
      </div>
    </section>`).join("");
  }

  function localDiscoveryStatusMarkup(message) {
    return `<div class="discovery-status" role="status">
      <span class="status-marker" aria-hidden="true"></span>
      <div><strong>Showing local movie categories</strong><p>${escapeHtml(message)} Regional shelves use the language and genre information in the local catalog; live discovery rankings are unavailable.</p></div>
      <a class="status-action" href="#/movies">Browse all films ${icons.arrow}</a>
    </div>`;
  }

  function discoveryDetailMarkup(movie) {
    const image = posterUrl(movie.posterUrl);
    const genres = Array.isArray(movie.genres) ? movie.genres.filter(Boolean).join(" · ") : "";
    const year = movieYear(movie);
    return `<section class="detail-hero" aria-labelledby="discovery-movie-title">
      ${image ? `<img class="detail-backdrop" src="${escapeHtml(image)}" alt="" aria-hidden="true">` : ""}
      <div class="detail-content">
        <div class="detail-poster">
          ${image ? `<img class="poster-image" src="${escapeHtml(image)}" alt="${escapeHtml(movie.title || "Film")} poster">` : ""}
          <span class="poster-fallback" ${image ? "hidden" : ""} aria-hidden="true">${escapeHtml(movie.title || "CineVerse")}</span>
        </div>
        <div class="detail-info">
          <a class="back-link" href="#/discover?category=trending">${icons.arrow} Movie discovery</a>
          <span class="eyebrow">TMDB movie discovery</span>
          <h1 id="discovery-movie-title">${escapeHtml(movie.title || "Untitled film")}</h1>
          <div class="detail-meta">
            ${genres ? `<span class="genre-chip">${escapeHtml(genres)}</span>` : ""}
            ${year ? `<span>${escapeHtml(year)}</span>` : ""}
            ${movie.rating != null ? `<span class="rating-pill">${icons.star}${escapeHtml(ratingText(movie.rating))}</span>` : ""}
          </div>
          <p class="detail-description">${escapeHtml(movie.description || "No overview is available for this film.")}</p>
          <a class="button button-primary" href="${discoveryBookingRoute(movie)}">${icons.play} Book Tickets</a>
          <p class="discovery-detail-note">Configured cinema listings are used when available. Otherwise, CineVerse offers clearly labelled demo-only showtimes.</p>
        </div>
      </div>
    </section>`;
  }

  async function renderDiscoveryDetail(movieId, viewId) {
    app.innerHTML = `<div class="page-wrap detail-body">${statePanel("Loading movie details", "Getting live movie information.", "")}</div>`;
    try {
      const movie = await api.getDiscoveryMovie(movieId);
      if (viewId !== state.viewSequence) return;
      app.innerHTML = discoveryDetailMarkup(movie);
      bindPosterFallbacks(app);
    } catch (error) {
      if (viewId !== state.viewSequence) return;
      app.innerHTML = `<div class="page-wrap" style="padding-top:58px">${statePanel("Movie details unavailable", error.message, `discovery-detail:${movieId}`)}</div>`;
    }
  }

  function bookingDates(selectedDate) {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    return Array.from({ length: 8 }, (_, offset) => {
      const date = new Date(today);
      date.setDate(today.getDate() + offset);
      const value = localDateValue(date);
      const label = offset === 0 ? `Today · ${formatShowDate(`${value}T12:00:00`)}`
        : formatShowDate(`${value}T12:00:00`);
      return `<option value="${value}" ${value === selectedDate ? "selected" : ""}>${escapeHtml(label)}</option>`;
    }).join("");
  }

  async function renderBookingStart(source, movieId, viewId) {
    state.bookingSource = source;
    state.bookingMovieId = movieId;
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    if (!state.bookingDate || state.bookingDate < localDateValue(today)) {
      state.bookingDate = localDateValue(today);
    }
    app.innerHTML = `<div class="page-wrap detail-body">${statePanel("Loading movie details", "Preparing your booking options.", "")}</div>`;
    try {
      const movie = source === "local" ? await api.getMovie(movieId)
        : source === "curated" ? curatedMovieByKey(movieId)
          : await api.getDiscoveryMovie(movieId);
      if (!movie) throw new Error("This curated fallback movie is unavailable.");
      if (viewId !== state.viewSequence) return;
      const genres = source === "tmdb" ? discoveryGenres(movie) : movie.genre;
      app.innerHTML = `<section class="detail-hero" aria-labelledby="booking-movie-title">
        ${posterUrl(movie.posterUrl) ? `<img class="detail-backdrop" src="${escapeHtml(posterUrl(movie.posterUrl))}" alt="" aria-hidden="true">` : ""}
        <div class="page-wrap booking-start-wrap">
          <a class="back-link" href="${source === "local" ? `#/movies/${encodeURIComponent(movieId)}` : `#/discover/${encodeURIComponent(movieId)}`}">${icons.arrow} Movie details</a>
          <span class="eyebrow">Book Tickets · ${source === "tmdb" ? "TMDB movie" : source === "curated" ? "Curated fallback movie" : "CineVerse movie"}</span>
          <h1 class="page-title" id="booking-movie-title">${escapeHtml(movie.title || "Movie")}</h1>
          <p class="booking-demo-note">Real screenings are shown when configured. Any simulated demo locations are clearly marked and are not real theatres.</p>
          <p class="booking-demo-note" role="status">${state.authUser
            ? `Showtimes will be shown for ${escapeHtml(state.selectedCity || "your selected city")}.`
            : `Sign in first to see showtimes in ${escapeHtml(state.selectedCity || "your selected city")}. Your city selection will be kept.`}</p>
          <div class="booking-start-controls">
            <label class="cinema-select-wrap" for="booking-date">Show date
              <select class="cinema-select" id="booking-date">${bookingDates(state.bookingDate)}</select>
            </label>
            <div class="booking-start-city"><span class="eyebrow">Selected city</span><strong>${escapeHtml(state.selectedCity || "Choose a city in the header")}</strong></div>
            <button class="button button-primary" type="button" data-find-booking-shows ${state.selectedCity ? "" : "disabled"}>${state.authUser ? "Find showtimes" : "Sign in to find showtimes"} ${icons.arrow}</button>
          </div>
          <p class="booking-start-feedback" id="booking-start-feedback" role="status" hidden></p>
          <div class="booking-show-results" id="booking-show-results" aria-live="polite"></div>
        </div>
      </section>`;
      document.getElementById("booking-date").addEventListener("change", (event) => {
        state.bookingDate = event.target.value;
        document.getElementById("booking-show-results").innerHTML = "";
      });
    } catch (error) {
      if (viewId !== state.viewSequence) return;
      app.innerHTML = `<div class="page-wrap detail-body">${statePanel("Movie details unavailable", error.message, source === "local" ? `detail:${movieId}` : `discovery-detail:${movieId}`)}</div>`;
    }
  }

  function curatedMovieByKey(key) {
    const movie = [...indianFallbackMovies, ...internationalFallbackMovies]
      .find((candidate) => candidate.bookingKey === key);
    return movie ? { ...movie, id: key, description: "Curated CineVerse fallback title." } : null;
  }

  async function findBookingShows(button) {
    if (!state.selectedCity) {
      const feedback = document.getElementById("booking-start-feedback");
      if (feedback) {
        feedback.textContent = "Choose a city from the selector in the header first.";
        feedback.hidden = false;
      }
      return;
    }
    if (!state.authUser) {
      location.hash = `#/login?next=${encodeURIComponent(`#/book/${state.bookingSource}/${state.bookingMovieId}`)}`;
      return;
    }
    button.disabled = true;
    button.textContent = "Checking configured and demo showtimes…";
    const feedback = document.getElementById("booking-start-feedback");
    const results = document.getElementById("booking-show-results");
    if (feedback) feedback.hidden = true;
    if (results) results.innerHTML = `<p class="online-search-loading" role="status">Checking show availability for ${escapeHtml(state.selectedCity)}…</p>`;
    try {
      const shows = await api.getBookingShows(
        state.bookingSource, state.bookingMovieId, state.selectedCity, state.bookingDate);
      const bookingShows = Array.isArray(shows) ? shows : [];
      const demoShows = bookingShows.some((show) => show.demo);
      const cinemaGroups = [...new Map(bookingShows.map((show) => [String(show.cinemaId || show.cinemaName), show])).values()];
      if (results) {
        results.innerHTML = bookingShows.length
          ? `${demoShows ? `<p class="booking-demo-note" role="status">Demo locations are simulated, not real theatres. Locally configured cinemas are shown alongside them where available.</p>` : `<p class="booking-demo-note" role="status">These are locally configured showtimes. Availability is checked again when you book.</p>`}
            <div class="cinema-list">${cinemaGroups.map((cinema) => {
              const cinemaShows = bookingShows.filter((show) => String(show.cinemaId || show.cinemaName) === String(cinema.cinemaId || cinema.cinemaName));
              return `<article class="cinema-block">
                <div class="cinema-name-row"><div><h3>${escapeHtml(cinema.cinemaName)}${cinema.demo ? " · DEMO" : ""}</h3><p class="cinema-address">${escapeHtml(cinema.city || state.selectedCity)}</p></div>
                  <span class="cinema-capacity">${escapeHtml(cinemaShows.length)} show${cinemaShows.length === 1 ? "" : "s"} · ${escapeHtml(cinema.totalSeatCapacity)} seats</span></div>
                <div class="show-times">${cinemaShows.map((show) => `<button class="show-time" type="button" data-show-id="${escapeHtml(show.id)}">
                  <strong>${escapeHtml(formatTime(show.startTime))}</strong>
                  <span>${escapeHtml(formatShowDate(show.startTime))} · ${escapeHtml(show.screenName)} · ${escapeHtml(priceText(show.ticketPrice))} · Select seats</span>
                </button>`).join("")}</div>
              </article>`;
            }).join("")}</div>`
          : emptyPanel("No showtimes on this date", "This movie has configured screenings on other dates, or today's demo sessions have already started. Choose another date or city.");
      }
    } catch (error) {
      if (feedback) {
        feedback.textContent = error.status === 401
          ? "Your sign-in expired. Log in again to continue."
          : `Showtimes could not be loaded: ${error.message}`;
        feedback.hidden = false;
      }
      if (error.status === 401) {
        location.hash = `#/login?next=${encodeURIComponent(`#/book/${state.bookingSource}/${state.bookingMovieId}`)}`;
      }
    } finally {
      button.disabled = !state.selectedCity;
      button.innerHTML = `Find showtimes ${icons.arrow}`;
    }
  }

  async function renderDiscoveryCategory(categorySlug, page, viewId) {
    const category = discoveryCategories.find((item) => item.slug === categorySlug);
    if (!category) {
      app.innerHTML = `<div class="page-wrap" style="padding-top:58px">${statePanel("Collection unavailable", "This movie category does not exist.", "home")}</div>`;
      return;
    }
    state.discoveryCategory = category.slug;
    state.discoveryPage = page;
    app.innerHTML = `<div class="page-wrap">
      <section class="catalog-intro">
        <a class="back-link" href="#/">${icons.arrow} Home</a>
        <span class="eyebrow">Live movie discovery</span>
        <h1 class="page-title">${escapeHtml(category.label)}.</h1>
        <p>Explore TMDB movies. Showtimes are offered only when a title is linked to a CineVerse cinema.</p>
      </section>
      <div id="discovery-category-status"></div>
      <section class="discovery-category-results" aria-label="${escapeHtml(category.label)} movies">
        <div id="discovery-category-results">${loadingCards(5)}</div>
        <nav class="discovery-pagination" aria-label="Movie pages" id="discovery-pagination"></nav>
      </section>
    </div>`;
    try {
      const status = await api.getDiscoveryStatus();
      if (viewId !== state.viewSequence) return;
      document.getElementById("discovery-category-status").innerHTML = discoveryStatusMarkup(status);
      if (!status.liveAvailable) {
        document.getElementById("discovery-category-results").innerHTML =
          statePanel("Live movies are unavailable", status.message, "home");
        return;
      }
      const response = await api.getDiscoveryMovies(category.slug, page, "");
      if (viewId !== state.viewSequence) return;
      const results = document.getElementById("discovery-category-results");
      results.innerHTML = response.results.length
        ? `<div class="movie-grid discovery-grid">${response.results.map(discoveryCard).join("")}</div>`
        : emptyPanel("No movies in this collection", "Try another movie category.");
      const pagination = document.getElementById("discovery-pagination");
      pagination.innerHTML = `${page > 1
        ? `<button class="button button-secondary" type="button" data-discovery-page="${page - 1}" data-category="${escapeHtml(category.slug)}">Previous</button>`
        : ""}<span>Page ${response.page} of ${response.totalPages}</span>${page < response.totalPages
        ? `<button class="button button-secondary" type="button" data-discovery-page="${page + 1}" data-category="${escapeHtml(category.slug)}">Next</button>`
        : ""}`;
      bindPosterFallbacks(results);
    } catch (error) {
      if (viewId !== state.viewSequence) return;
      document.getElementById("discovery-category-results").innerHTML =
        statePanel("Movies couldn't load", error.message, `discovery-category:${category.slug}:${page}`);
    }
  }

  function bindPosterFallbacks(root = app) {
    root.querySelectorAll(".poster-image").forEach((image) => {
      image.addEventListener("error", () => {
        image.hidden = true;
        const fallback = image.parentElement.querySelector(".poster-fallback");
        if (fallback) fallback.hidden = false;
      }, { once: true });
    });
    root.querySelectorAll(".hero-image, .detail-backdrop").forEach((image) => {
      image.addEventListener("error", () => image.remove(), { once: true });
    });
  }

  function heroMarkup(movie, index = 0, count = 1, label = "FEATURED") {
    if (!movie) {
      return `<section class="hero"><div class="hero-content"><div class="hero-copy">
        <span class="eyebrow hero-kicker">A better way to go to the movies</span>
        <h1 class="hero-title">The next great story starts here.</h1>
        <p class="hero-description">Explore films playing near you and find a screening that fits your night.</p>
        <div class="hero-actions"><a class="button button-primary" href="#/movies">Explore movies ${icons.arrow}</a></div>
      </div></div></section>`;
    }
    const image = posterUrl(movie.posterUrl);
    const description = movie.description || "A story worth seeing on the big screen.";
    const genre = movie.genre || (Array.isArray(movie.genres) ? movie.genres.filter(Boolean).join(" · ") : "");
    const duration = durationText(movie.durationMinutes);
    const year = movieYear(movie);
    const detailHref = movie.source === "TMDB"
      ? `#/discover/${encodeURIComponent(movie.id)}`
      : `#/movies/${encodeURIComponent(movie.localMovieId || movie.id)}`;
    return `<section class="hero" aria-labelledby="featured-title">
      ${image ? `<img class="hero-image" src="${escapeHtml(image)}" alt="" fetchpriority="high">` : ""}
      <div class="hero-content"><div class="hero-copy">
        <span class="eyebrow hero-kicker">The CineVerse spotlight</span>
        <h1 class="hero-title" id="featured-title">${escapeHtml(movie.title || "Featured film")}</h1>
        <div class="hero-meta">
          <span class="rating-pill">${icons.star}${escapeHtml(ratingText(movie.rating))}</span>
          ${genre ? `<span>${escapeHtml(genre)}</span>` : ""}
          ${duration ? `<span>${escapeHtml(duration)}</span>` : ""}
          ${year ? `<span>${escapeHtml(year)}</span>` : ""}
        </div>
        <p class="hero-description">${escapeHtml(description)}</p>
        <div class="hero-actions">
          <a class="button button-primary" href="#/movies">Explore Movies ${icons.arrow}</a>
          <a class="button button-secondary" href="${detailHref}">Film details ${icons.arrow}</a>
        </div>
      </div></div>
      <div class="hero-carousel-controls">
        ${count > 1 ? `<button class="hero-control-button" type="button" data-spotlight-step="-1" aria-label="Previous spotlight movie">‹</button>` : ""}
        <div class="hero-index"><strong>${String(index + 1).padStart(2, "0")}</strong><span>/</span> ${label}</div>
        ${count > 1 ? `<button class="hero-control-button" type="button" data-spotlight-step="1" aria-label="Next spotlight movie">›</button>` : ""}
      </div>
    </section>`;
  }

  function stopSpotlightTimer() {
    if (state.spotlightTimer !== null) {
      window.clearInterval(state.spotlightTimer);
      state.spotlightTimer = null;
    }
  }

  function startSpotlightTimer() {
    stopSpotlightTimer();
    const hero = app.querySelector(".hero");
    if (state.spotlightMovies.length < 2
        || window.matchMedia("(prefers-reduced-motion: reduce)").matches
        || document.hidden
        || hero?.matches(":hover")
        || hero?.contains(document.activeElement)) return;
    state.spotlightTimer = window.setInterval(() => {
      renderSpotlightSlide(state.spotlightIndex + 1, true);
    }, 6500);
  }

  function renderSpotlightSlide(index, animate) {
    const currentHero = app.querySelector(".hero");
    if (!currentHero || !state.spotlightMovies.length) return;
    state.spotlightIndex = (index + state.spotlightMovies.length) % state.spotlightMovies.length;
    const container = document.createElement("div");
    container.innerHTML = heroMarkup(
      state.spotlightMovies[state.spotlightIndex],
      state.spotlightIndex,
      state.spotlightMovies.length,
      state.spotlightLabel
    );
    const nextHero = container.firstElementChild;
    if (!nextHero) return;
    if (animate) nextHero.classList.add("is-changing");
    currentHero.replaceWith(nextHero);
    bindPosterFallbacks(nextHero);
    nextHero.addEventListener("mouseenter", stopSpotlightTimer);
    nextHero.addEventListener("mouseleave", startSpotlightTimer);
    nextHero.addEventListener("focusin", stopSpotlightTimer);
    nextHero.addEventListener("focusout", (event) => {
      if (!nextHero.contains(event.relatedTarget)) startSpotlightTimer();
    });
  }

  function setSpotlightMovies(movies, viewId, label = "FEATURED") {
    if (viewId !== state.viewSequence) return;
    const seen = new Set();
    state.spotlightMovies = movies.filter((movie) => {
      const key = String(movie.id || movie.title || "");
      if (!key || seen.has(key)) return false;
      seen.add(key);
      return true;
    }).slice(0, 10);
    state.spotlightIndex = 0;
    state.spotlightLabel = label;
    renderSpotlightSlide(0, false);
    startSpotlightTimer();
  }

  function renderHomeShell(movie, movies, loading = false, discoveryStatus = null) {
    stopSpotlightTimer();
    const discoveryContent = discoveryStatus
      ? discoveryStatus.liveAvailable
        ? `${discoveryStatusMarkup(discoveryStatus)}<p id="home-featured-error" class="online-fallback" role="status" hidden></p>
          ${featuredHomeSection("indian-movies", "Indian Movies", [], { loading: true })}
          ${featuredHomeSection("international-movies", "International Movies", [], { loading: true })}
          ${homeDiscoveryCategories.map((slug) => {
          const category = discoveryCategories.find((item) => item.slug === slug);
          return category ? discoveryRailMarkup(category) : "";
        }).join("")}`
        : `${localDiscoveryStatusMarkup(discoveryStatus.message)}
          ${featuredLocalRailsMarkup(movies)}
          ${localDiscoveryRailsMarkup(movies)}`
      : "";
    app.innerHTML = `${heroMarkup(movie)}
      <div class="page-wrap home-body">
        ${loading ? `<div class="home-loading">${loadingCards(5)}</div>` : discoveryContent}
        ${loading ? "" : `<section class="notice" aria-label="CineVerse service information">${icons.shield}<span>Explore movies, compare showtimes and reserve available seats. Bookings are demos only; no payment is processed.</span></section>`}
      </div>`;
    bindPosterFallbacks(app);
  }

  async function renderHome(viewId) {
    stopSpotlightTimer();
    state.spotlightMovies = [];
    state.spotlightIndex = 0;
    state.spotlightLabel = "FEATURED";
    renderHomeShell(null, [], true);
    try {
      const discoveryStatusRequest = api.getDiscoveryStatus().catch((error) => ({
        liveAvailable: false,
        message: `TMDB status is unavailable: ${error.message}`,
        region: "India"
      }));
      const [movies, discoveryStatus] = await Promise.all([api.getMovies(), discoveryStatusRequest]);
      if (viewId !== state.viewSequence) return;
      state.movies = Array.isArray(movies) ? movies : [];
      state.discoveryStatus = discoveryStatus;
      renderHomeShell(state.movies[0] || null, state.movies, false, discoveryStatus);
      setSpotlightMovies(state.movies, viewId);
      if (discoveryStatus.liveAvailable) {
        loadHomeFeaturedSections(state.movies, viewId);
        homeDiscoveryCategories.forEach((slug) => {
          const category = discoveryCategories.find((item) => item.slug === slug);
          if (category) loadDiscoveryRail(category, viewId);
        });
      }
    } catch (error) {
      if (viewId !== state.viewSequence) return;
      const fallbackStatus = {
        liveAvailable: false,
        message: `Movie catalog unavailable: ${error.message}`
      };
      renderHomeShell(null, state.movies, false, fallbackStatus);
    }
  }

  function genreOptions() {
    const selected = state.catalogGenre;
    return `<option value="">All genres</option>${state.catalogGenres.map((genre) =>
      `<option value="${escapeHtml(genre)}" ${genre === selected ? "selected" : ""}>${escapeHtml(genre)}</option>`
    ).join("")}`;
  }

  function languageOptions() {
    const selected = state.catalogLanguage;
    return `<option value="">All languages</option>${state.catalogLanguages.map((language) =>
      `<option value="${escapeHtml(language)}" ${language === selected ? "selected" : ""}>${escapeHtml(language)}</option>`
    ).join("")}`;
  }

  function genreValues(movie) {
    if (Array.isArray(movie.genres)) return movie.genres.filter(Boolean).map(String);
    return String(movie.genre || "").split(/\s*[·,|]\s*/).filter(Boolean);
  }

  function canonicalGenre(value) {
    const genre = String(value || "").trim().toLocaleLowerCase();
    if (genre === "musical") return "music";
    if (genre === "sci-fi") return "science fiction";
    return genre;
  }

  function renderCatalogResults(movies, error = "") {
    const results = document.getElementById("catalog-results");
    const count = document.getElementById("result-count");
    if (!results || !count) return;
    if (error) {
      count.textContent = "";
      results.innerHTML = statePanel("Search couldn't load", error, "catalog");
      return;
    }
    const query = state.catalogQuery.trim().toLocaleLowerCase();
    const knownLanguages = new Set(catalogLanguageChoices
      .filter((language) => language !== "Other")
      .map((language) => language.toLocaleLowerCase()));
    const matchesFilters = (movie) => {
      const genres = genreValues(movie).map(canonicalGenre);
      const language = languageLabel(movie.language).toLocaleLowerCase();
      return (!state.catalogGenre || genres.includes(canonicalGenre(state.catalogGenre)))
        && (!state.catalogLanguage
          || (state.catalogLanguage === "Other" ? !knownLanguages.has(language)
            : language === languageLabel(state.catalogLanguage).toLocaleLowerCase()));
    };
    const visible = movies
      .filter((movie) => !query || String(movie.title || "").toLocaleLowerCase().includes(query))
      .filter(matchesFilters)
      .sort((left, right) => {
        const order = String(left.title || "").localeCompare(String(right.title || ""), undefined, { sensitivity: "base" });
        return state.catalogSort === "desc" ? -order : order;
      });
    const localMarkup = visible.length
      ? `<div class="movie-grid">${visible.map(movieCard).join("")}</div>`
      : emptyPanel("No matching local movies", "Try a different title, genre, or language. Online results may still be available below.");
    const remoteMovies = state.catalogRemoteMovies
      .filter((movie) => !query || String(movie.title || "").toLocaleLowerCase().includes(query))
      .filter(matchesFilters)
      .filter((movie) => !visible.some((local) => Number(local.id) === Number(movie.localMovieId)
        || String(local.title || "").toLocaleLowerCase() === String(movie.title || "").toLocaleLowerCase()))
      .sort((left, right) => {
        const order = String(left.title || "").localeCompare(String(right.title || ""), undefined, { sensitivity: "base" });
        return state.catalogSort === "desc" ? -order : order;
      });
    const onlineMarkup = query
      ? `<section class="online-search-results" aria-label="Online movie search results">
          <div class="section-heading"><div><span class="eyebrow">TMDB discovery</span><h2>Online results</h2></div><span class="result-count">${remoteMovies.length} ${remoteMovies.length === 1 ? "film" : "films"}</span></div>
          ${state.catalogRemoteLoading
            ? `<p class="online-search-loading" role="status">Searching TMDB for “${escapeHtml(state.catalogQuery.trim())}”…</p>`
            : state.catalogRemoteMessage
            ? `<p class="online-fallback" role="status">${escapeHtml(state.catalogRemoteMessage)} ${visible.length ? "Matching local results are shown above." : "The local catalog above remains available; try another title or check back later."}</p>`
            : remoteMovies.length
              ? `<div class="movie-grid discovery-grid">${remoteMovies.map(discoveryCard).join("")}</div>`
              : emptyPanel("No online matches", "Try another title or adjust the genre and language filters.")}
        </section>`
      : "";
    count.textContent = query && state.catalogRemoteLoading
      ? `${visible.length} local · searching online…`
      : query
      ? `${visible.length} local · ${remoteMovies.length} online`
      : `${visible.length} ${visible.length === 1 ? "film" : "films"}`;
    results.innerHTML = `<section aria-label="Local cinema catalog"><div class="section-heading"><div><span class="eyebrow">Configured in CineVerse</span><h2>Local movies</h2></div></div>${localMarkup}</section>${onlineMarkup}`;
    bindPosterFallbacks(results);
  }

  function updateCatalogFilterOptions() {
    const localGenres = state.catalogMovies.flatMap(genreValues);
    const remoteGenres = state.catalogRemoteMovies.flatMap(genreValues);
    state.catalogGenres = [...new Set([...catalogGenreChoices, ...localGenres, ...remoteGenres, state.catalogGenre]
      .filter((value) => String(value || "").trim()))]
      .sort((left, right) => left.localeCompare(right));
    state.catalogLanguages = [...new Set([
      ...catalogLanguageChoices,
      ...state.catalogMovies.map((movie) => languageLabel(movie.language)),
      ...state.catalogRemoteMovies.map((movie) => languageLabel(movie.language)),
      languageLabel(state.catalogLanguage)
    ].filter(Boolean))].sort((left, right) => left.localeCompare(right));
    const genre = document.getElementById("genre-filter");
    const language = document.getElementById("language-filter");
    if (genre) genre.innerHTML = genreOptions();
    if (language) language.innerHTML = languageOptions();
  }

  async function searchOnlineCatalog(query, viewId) {
    const searchSequence = ++state.catalogSearchSequence;
    state.catalogRemoteMovies = [];
    state.catalogRemoteMessage = "";
    state.catalogRemoteLoading = false;
    if (!query.trim()) {
      updateCatalogFilterOptions();
      renderCatalogResults(state.catalogMovies);
      return;
    }
    state.catalogRemoteLoading = true;
    renderCatalogResults(state.catalogMovies);
    try {
      const status = state.discoveryStatus || await api.getDiscoveryStatus();
      state.discoveryStatus = status;
      if (!status.liveAvailable) {
        state.catalogRemoteMessage = `Online search is unavailable. ${status.message || "TMDB is not configured."}`;
      } else {
        const response = await api.getDiscoveryMovies("all", 1, query.trim());
        if (viewId !== state.viewSequence || searchSequence !== state.catalogSearchSequence) return;
        state.catalogRemoteMovies = Array.isArray(response.results) ? response.results : [];
        if (!state.catalogRemoteMovies.length) state.catalogRemoteMessage = "";
      }
    } catch (error) {
      if (viewId !== state.viewSequence || searchSequence !== state.catalogSearchSequence) return;
      state.catalogRemoteMessage = `Online search is temporarily unavailable (${error.message}).`;
    }
    if (viewId !== state.viewSequence || searchSequence !== state.catalogSearchSequence) return;
    state.catalogRemoteLoading = false;
    updateCatalogFilterOptions();
    renderCatalogResults(state.catalogMovies);
  }

  function renderCatalogShell(viewId) {
    state.catalogSearchSequence += 1;
    if (state.catalogSearchTimer) window.clearTimeout(state.catalogSearchTimer);
    state.catalogQuery = "";
    state.catalogGenre = "";
    state.catalogLanguage = "";
    state.catalogSort = "asc";
    state.catalogRemoteMovies = [];
    state.catalogRemoteMessage = "";
    state.catalogRemoteLoading = false;
    app.innerHTML = `<div class="page-wrap">
      <section class="catalog-intro">
        <span class="eyebrow">A world of stories</span>
        <h1 class="page-title">Find your next film.</h1>
        <p>Browse every available movie, search by title, and combine genre and language filters to find your next screening.</p>
      </section>
      <section class="catalog-controls" aria-label="Filter movies">
        <label class="search-field">
          ${icons.search}
          <input class="search-input" id="movie-search" type="search" maxlength="150" placeholder="Search by movie title" autocomplete="off" aria-label="Search movie titles">
        </label>
        <label class="select-wrap">
          <select class="genre-select" id="genre-filter" aria-label="Filter by genre">${genreOptions()}</select>
        </label>
        <label class="select-wrap">
          <select class="genre-select" id="language-filter" aria-label="Filter by language">${languageOptions()}</select>
        </label>
        <label class="select-wrap">
          <select class="genre-select sort-select" id="sort-filter" aria-label="Sort movies">
            <option value="asc">Title: A–Z</option>
            <option value="desc">Title: Z–A</option>
          </select>
        </label>
        <button class="button button-secondary clear-filters" id="clear-filters" type="button">Clear filters</button>
        <span class="result-count" id="result-count" aria-live="polite">Loading catalog…</span>
      </section>
      <section class="catalog-results" id="catalog-results" aria-label="Movie catalog">${loadingCards(8)}</section>
    </div>`;
    const input = document.getElementById("movie-search");
    const genre = document.getElementById("genre-filter");
    const language = document.getElementById("language-filter");
    const sort = document.getElementById("sort-filter");
    input.addEventListener("input", () => {
      state.catalogQuery = input.value;
      state.catalogRemoteMovies = [];
      state.catalogRemoteMessage = "";
      state.catalogRemoteLoading = Boolean(state.catalogQuery.trim());
      renderCatalogResults(state.catalogMovies);
      if (state.catalogSearchTimer) window.clearTimeout(state.catalogSearchTimer);
      state.catalogSearchTimer = window.setTimeout(() => searchOnlineCatalog(state.catalogQuery, viewId), 350);
    });
    genre.addEventListener("change", () => {
      state.catalogGenre = genre.value;
      renderCatalogResults(state.catalogMovies);
    });
    language.addEventListener("change", () => {
      state.catalogLanguage = language.value;
      renderCatalogResults(state.catalogMovies);
    });
    sort.addEventListener("change", () => {
      state.catalogSort = sort.value;
      renderCatalogResults(state.catalogMovies);
    });
    document.getElementById("clear-filters").addEventListener("click", () => {
      input.value = "";
      genre.value = "";
      language.value = "";
      sort.value = "asc";
      state.catalogQuery = "";
      state.catalogGenre = "";
      state.catalogLanguage = "";
      state.catalogSort = "asc";
      state.catalogRemoteMovies = [];
      state.catalogRemoteMessage = "";
      state.catalogRemoteLoading = false;
      state.catalogSearchSequence += 1;
      if (state.catalogSearchTimer) window.clearTimeout(state.catalogSearchTimer);
      updateCatalogFilterOptions();
      renderCatalogResults(state.catalogMovies);
      input.focus();
    });
    return { input, genre, language, sort };
  }

  async function renderCatalog(viewId) {
    const controls = renderCatalogShell(viewId);
    try {
      const movies = await api.getMovies();
      if (viewId !== state.viewSequence) return;
      state.movies = Array.isArray(movies) ? movies : [];
      state.catalogMovies = state.movies;
      updateCatalogFilterOptions();
      renderCatalogResults(state.catalogMovies);
      if (location.hash.includes("?")) {
        const params = new URLSearchParams(location.hash.split("?")[1]);
        const initialSearch = params.get("search") || "";
        controls.input.value = initialSearch;
        state.catalogQuery = initialSearch;
        if (initialSearch) {
          state.catalogSearchTimer = window.setTimeout(() => searchOnlineCatalog(initialSearch, viewId), 0);
        }
        const initialGenre = params.get("genre") || "";
        if (initialGenre && state.catalogGenres.includes(initialGenre)) {
          controls.genre.value = initialGenre;
          state.catalogGenre = initialGenre;
        }
        const initialLanguage = params.get("language") || "";
        if (initialLanguage && state.catalogLanguages.includes(initialLanguage)) {
          controls.language.value = initialLanguage;
          state.catalogLanguage = initialLanguage;
        }
        const initialSort = params.get("sort") || "asc";
        if (initialSort === "desc") {
          controls.sort.value = "desc";
          state.catalogSort = "desc";
        }
        renderCatalogResults(state.catalogMovies);
      }
    } catch (error) {
      if (viewId !== state.viewSequence) return;
      document.getElementById("result-count").textContent = "";
      document.getElementById("catalog-results").innerHTML = statePanel("Catalog unavailable", error.message, "catalog");
    }
  }

  function detailMarkup(movie) {
    const image = posterUrl(movie.posterUrl);
    const genre = movie.genre || "";
    const duration = durationText(movie.durationMinutes);
    const year = movieYear(movie);
    return `<section class="detail-hero" aria-labelledby="movie-title">
      ${image ? `<img class="detail-backdrop" src="${escapeHtml(image)}" alt="" aria-hidden="true">` : ""}
      <div class="detail-content">
        <div class="detail-poster">
          ${image ? `<img class="poster-image" src="${escapeHtml(image)}" alt="${escapeHtml(movie.title || "Film")} poster">` : ""}
          <span class="poster-fallback" ${image ? "hidden" : ""} aria-hidden="true">${escapeHtml(movie.title || "CineVerse")}</span>
        </div>
        <div class="detail-info">
          <a class="back-link" href="#/movies">${icons.arrow} All movies</a>
          <span class="eyebrow">The film</span>
          <h1 id="movie-title">${escapeHtml(movie.title || "Untitled film")}</h1>
          <div class="detail-meta">
            ${genre ? `<span class="genre-chip">${escapeHtml(genre)}</span>` : ""}
            ${movie.language ? `<span>${escapeHtml(movie.language)}</span>` : ""}
            ${duration ? `<span>${escapeHtml(duration)}</span>` : ""}
            ${year ? `<span>${escapeHtml(year)}</span>` : ""}
            <span class="rating-pill">${icons.star}${escapeHtml(ratingText(movie.rating))}</span>
          </div>
          <p class="detail-description">${escapeHtml(movie.description || "More information about this film has not been provided.")}</p>
          <a class="button button-primary" href="#/book/local/${encodeURIComponent(movie.id)}">${icons.play} Book Tickets</a>
          <a class="button button-secondary" href="#/movies/${encodeURIComponent(movie.id)}/shows">Compare configured showtimes</a>
        </div>
      </div>
    </section>
    <div class="page-wrap detail-body">
      <section class="notice" aria-label="Booking information">${icons.shield}<span>Select a showtime to choose available seats. Confirmed bookings are saved in this browser; no payment is collected.</span></section>
    </div>`;
  }

  async function renderDetail(movieId, viewId) {
    app.innerHTML = `<div class="hero-skeleton" aria-label="Loading movie details"></div><div class="page-wrap detail-body">${loadingCards(1)}</div>`;
    try {
      const movie = await api.getMovie(movieId);
      if (viewId !== state.viewSequence) return;
      app.innerHTML = detailMarkup(movie);
      bindPosterFallbacks(app);
    } catch (error) {
      if (viewId !== state.viewSequence) return;
      app.innerHTML = `<div class="page-wrap" style="padding-top:58px">${statePanel("Film details unavailable", error.message, `detail:${movieId}`)}</div>`;
    }
  }

  function cinemaForShow(show, cinemas) {
    return cinemas.find((cinema) => Number(cinema.id) === Number(show.cinemaId));
  }

  function groupedShowMarkup(shows, cinemas) {
    const scheduled = shows.filter((show) => show.status === "SCHEDULED")
      .sort((a, b) => new Date(a.startTime) - new Date(b.startTime));
    state.shownRecords = new Map(scheduled.map((show) => [String(show.id), show]));
    if (!scheduled.length) {
      return emptyPanel("No screenings listed", "There are no scheduled showtimes for this film at this selection right now.");
    }
    const groups = new Map();
    scheduled.forEach((show) => {
      const key = String(show.cinemaId);
      if (!groups.has(key)) groups.set(key, []);
      groups.get(key).push(show);
    });
    return [...groups.entries()].map(([cinemaId, cinemaShows]) => {
      const cinema = cinemaForShow(cinemaShows[0], cinemas);
      const cinemaName = cinemaShows[0].cinemaName || (cinema && cinema.name) || "Cinema";
      const address = cinema ? [cinema.address, cinema.city].filter(Boolean).join(", ") : cinemaShows[0].city || "";
      const screenNames = [...new Set(cinemaShows.map((show) => show.screenName).filter(Boolean))];
      const capacity = cinemaShows[0].totalSeatCapacity;
      return `<article class="cinema-block">
        <div class="cinema-name-row">
          <div><h3>${escapeHtml(cinemaName)}${cinemaShows[0].demo ? " · DEMO ONLY" : ""}</h3><p class="cinema-address">${escapeHtml(address || "Location details not listed")}</p></div>
          <span class="cinema-capacity">${screenNames.length ? escapeHtml(screenNames.join(" · ")) : "Screening room"}${capacity ? ` · ${escapeHtml(capacity)} seats` : ""}</span>
        </div>
        <div class="show-times">${cinemaShows.map((show) => `<button class="show-time" type="button" data-show-id="${escapeHtml(show.id)}" aria-label="Choose seats for the ${escapeHtml(formatTime(show.startTime))} show at ${escapeHtml(cinemaName)}">
          <strong>${escapeHtml(formatTime(show.startTime))}</strong>
          <span>${escapeHtml(formatShowDate(show.startTime))}${show.demo ? " · DEMO" : ""}${show.ticketPrice != null ? ` · ${escapeHtml(priceText(show.ticketPrice))}` : ""} · Select seats</span>
        </button>`).join("")}</div>
      </article>`;
    }).join("");
  }

  function showDateOptions(shows, selectedDate) {
    const dates = [...new Set(shows
      .filter((show) => show.status === "SCHEDULED")
      .map((show) => String(show.startTime || "").slice(0, 10))
      .filter((date) => /^\d{4}-\d{2}-\d{2}$/.test(date)))]
      .sort();
    return `<option value="all" ${selectedDate === "all" ? "selected" : ""}>All dates</option>${dates.map((date) => {
      const label = new Intl.DateTimeFormat(undefined, { weekday: "short", month: "short", day: "numeric" })
        .format(new Date(`${date}T12:00:00`));
      return `<option value="${escapeHtml(date)}" ${date === selectedDate ? "selected" : ""}>${escapeHtml(label)}</option>`;
    }).join("")}`;
  }

  function renderShowSchedule() {
    const dateSelect = document.getElementById("show-date-filter");
    const list = document.getElementById("cinema-show-list");
    if (!dateSelect || !list) return;
    const availableDates = new Set(state.showSchedule.map((show) => String(show.startTime || "").slice(0, 10)));
    if (state.showDate !== "all" && !availableDates.has(state.showDate)) state.showDate = "all";
    dateSelect.innerHTML = showDateOptions(state.showSchedule, state.showDate);
    dateSelect.value = state.showDate;
    const filteredShows = state.showDate === "all"
      ? state.showSchedule
      : state.showSchedule.filter((show) => String(show.startTime || "").startsWith(state.showDate));
    list.innerHTML = groupedShowMarkup(filteredShows, state.showScheduleCinemas);
  }

  function cinemasForSelectedCity(cinemas) {
    if (!state.selectedCity) return cinemas;
    return cinemas.filter((cinema) => String(cinema.city || "").toLocaleLowerCase() === state.selectedCity.toLocaleLowerCase());
  }

  function showsForCinemas(shows, cinemas) {
    const cinemaIds = new Set(cinemas.map((cinema) => String(cinema.id)));
    return shows.filter((show) => cinemaIds.has(String(show.cinemaId)));
  }

  function renderShowsPage(movie, cinemas, shows, selectedCinema, message = "") {
    const cityCinemas = cinemasForSelectedCity(cinemas);
    const cityShows = showsForCinemas(shows, cityCinemas);
    state.showSchedule = cityShows;
    state.showScheduleCinemas = cityCinemas;
    state.showDate = "all";
    const options = `<option value="all" ${selectedCinema === "all" ? "selected" : ""}>All cinemas</option>${cityCinemas.map((cinema) =>
      `<option value="${escapeHtml(cinema.id)}" ${String(cinema.id) === String(selectedCinema) ? "selected" : ""}>${escapeHtml(cinema.name)}${cinema.city ? ` — ${escapeHtml(cinema.city)}` : ""}</option>`
    ).join("")}`;
    app.innerHTML = `<section class="detail-hero" aria-labelledby="show-movie-title">
      ${posterUrl(movie.posterUrl) ? `<img class="detail-backdrop" src="${escapeHtml(posterUrl(movie.posterUrl))}" alt="" aria-hidden="true">` : ""}
      <div class="page-wrap" style="position:relative;padding-top:38px">
        <a class="back-link" href="#/movies/${encodeURIComponent(movie.id)}">${icons.arrow} Film details</a>
        <span class="eyebrow">Choose your cinema</span>
        <h1 class="page-title" id="show-movie-title" style="max-width:760px;margin:12px 0 8px;font-size:clamp(38px,5vw,58px)">${escapeHtml(movie.title || "Film showtimes")}</h1>
        <p style="max-width:590px;margin:0;color:#aaa3a1;font-size:13px;line-height:1.7">Compare configured cinema screenings. Any demo-only showtime is clearly labelled and does not represent real theatre inventory.</p>
      </div>
    </section>
    <div class="page-wrap detail-body">
      <section class="screening-panel" aria-label="Cinema and showtime selection">
        <div class="screening-intro">
          <span class="eyebrow">Screenings</span>
          <h2>Make it a movie night.</h2>
          <p>Showtimes come from the current schedule. Availability may change.</p>
          <div class="schedule-filters">
            <div class="cinema-select-wrap"><label for="cinema-filter">Cinema</label><select class="cinema-select" id="cinema-filter">${options}</select></div>
            <div class="cinema-select-wrap"><label for="show-date-filter">Show date</label><select class="cinema-select" id="show-date-filter">${showDateOptions(shows, state.showDate)}</select></div>
          </div>
        </div>
        <div>
          <div class="cinema-list" id="cinema-show-list">${message || (cityCinemas.length
            ? groupedShowMarkup(cityShows, cityCinemas)
            : emptyPanel("No cinemas configured in this city", `CineVerse has no configured cinemas for ${escapeHtml(state.selectedCity)}. Choose another city to see its listed screenings.`))}</div>
          <div class="notice">${icons.shield}<span>Choose a showtime to view seat availability. Demo-only screenings are simulations; no real payment is processed.</span></div>
        </div>
      </section>
    </div>`;
    const select = document.getElementById("cinema-filter");
    select.addEventListener("change", () => loadSelectedCinema(movie, cinemas, select.value, state.viewSequence));
    document.getElementById("show-date-filter").addEventListener("change", (event) => {
      state.showDate = event.target.value;
      renderShowSchedule();
    });
  }

  async function loadSelectedCinema(movie, cinemas, cinemaId, viewId) {
    const list = document.getElementById("cinema-show-list");
    if (!list) return;
    if (cinemaId === "all") {
      const select = document.getElementById("cinema-filter");
      select.disabled = true;
      list.innerHTML = `<div class="state" role="status"><div class="state-inner"><h2>Refreshing showtimes</h2><p>Checking the movie schedule.</p></div></div>`;
      try {
        const shows = await api.getShowsByMovie(movie.id);
        if (viewId !== state.viewSequence) return;
        state.showScheduleCinemas = cinemasForSelectedCity(cinemas);
        state.showSchedule = showsForCinemas(shows, state.showScheduleCinemas);
        renderShowSchedule();
      } catch (error) {
        if (viewId !== state.viewSequence) return;
        list.innerHTML = statePanel("Showtimes unavailable", error.message, `shows:${movie.id}`);
      } finally {
        if (viewId === state.viewSequence && select) select.disabled = false;
      }
      return;
    }
    const select = document.getElementById("cinema-filter");
    if (select) select.disabled = true;
    list.innerHTML = `<div class="state" role="status"><div class="state-inner"><h2>Checking this cinema</h2><p>Loading current showtimes for ${escapeHtml(movie.title)}.</p></div></div>`;
    try {
      const cinemaShows = await api.getShowsByCinema(cinemaId);
      if (viewId !== state.viewSequence) return;
      const matchedShows = cinemaShows.filter((show) => Number(show.movieId) === Number(movie.id));
      state.showSchedule = matchedShows;
      state.showScheduleCinemas = cinemas;
      renderShowSchedule();
    } catch (error) {
      if (viewId !== state.viewSequence) return;
      list.innerHTML = statePanel("Cinema schedule unavailable", error.message, `cinema:${movie.id}:${cinemaId}`);
    } finally {
      if (viewId === state.viewSequence && select) select.disabled = false;
    }
  }

  async function renderShows(movieId, viewId) {
    app.innerHTML = `<div class="hero-skeleton" aria-label="Loading showtimes"></div><div class="page-wrap detail-body">${statePanel("Loading cinemas", "Getting the current movie schedule and cinema list.", "")}</div>`;
    try {
      const [movie, shows, cinemas] = await Promise.all([
        api.getMovie(movieId),
        api.getShowsByMovie(movieId),
        api.getCinemas()
      ]);
      if (viewId !== state.viewSequence) return;
      const availableCinemas = Array.isArray(cinemas) ? cinemas : [];
      renderCityOptions(availableCinemas);
      renderShowsPage(movie, availableCinemas, Array.isArray(shows) ? shows : [], "all");
    } catch (error) {
      if (viewId !== state.viewSequence) return;
      app.innerHTML = `<div class="page-wrap" style="padding-top:58px">${statePanel("Screenings couldn't load", error.message, `shows:${movieId}`)}</div>`;
    }
  }

  function savedBookingEntries() {
    try {
      const stored = JSON.parse(localStorage.getItem("cineverse-booking-references") || "[]");
      if (Array.isArray(stored)) {
        const unique = new Map();
        stored.forEach((entry) => {
          if (entry && /^\d+$/.test(String(entry.id)) &&
              /^CV-[A-F0-9-]{36}$/.test(String(entry.reference))) {
            unique.set(String(entry.reference), { id: String(entry.id), reference: String(entry.reference) });
          }
        });
        return [...unique.values()];
      }
    } catch (error) {
      // Continue with the in-memory list if browser storage is unavailable.
    }
    return state.bookingEntries;
  }

  function saveBooking(booking) {
    state.bookingEntries = [...new Map([
      ...savedBookingEntries(),
      { id: String(booking.id), reference: String(booking.bookingReference) }
    ].map((entry) => [entry.reference, entry])).values()];
    try {
      localStorage.setItem("cineverse-booking-references", JSON.stringify(state.bookingEntries));
    } catch (error) {
      // The booking is still confirmed by the server for this session.
    }
  }

  function bookingCodeMarkup(reference) {
    const size = 21;
    const cells = [];
    const isFinderCell = (x, y) =>
      (x < 7 && y < 7) || (x >= size - 7 && y < 7) || (x < 7 && y >= size - 7);
    const finderIsDark = (x, y) => {
      const localX = x >= size - 7 ? x - (size - 7) : x;
      const localY = y >= size - 7 ? y - (size - 7) : y;
      return localX === 0 || localX === 6 || localY === 0 || localY === 6 ||
        (localX >= 2 && localX <= 4 && localY >= 2 && localY <= 4);
    };
    const seed = [...String(reference)].reduce((value, character) => (value * 31 + character.charCodeAt(0)) >>> 0, 7);
    for (let y = 0; y < size; y += 1) {
      for (let x = 0; x < size; x += 1) {
        const dark = isFinderCell(x, y)
          ? finderIsDark(x, y)
          : ((Math.imul(seed ^ (x + 1) * 0x45d9f3b, (y + 1) * 0x27d4eb2d) >>> ((x + y) % 16)) & 1) === 1;
        if (dark) cells.push(`<rect x="${x + 4}" y="${y + 4}" width="1" height="1"/>`);
      }
    }
    return `<svg class="booking-code" viewBox="0 0 29 29" role="img" aria-label="Illustrative booking code; not a scannable QR code"><rect width="29" height="29" rx="2" fill="#fff"/><g fill="#171617">${cells.join("")}</g></svg>`;
  }

  function bookingAftercareMarkup(booking) {
    if (booking.aftercareType) {
      const requestType = booking.aftercareType === "REFUND" ? "Demo refund" : "Demo ticket exchange";
      const message = booking.aftercareType === "REFUND"
        ? "Your refund will be processed soon. Simulated status only; no real refund has been initiated."
        : "Your demo ticket exchange request is recorded for review. No exchange has been processed.";
      return `<div class="booking-aftercare-status" role="status"><strong>${requestType} request: ${escapeHtml(booking.aftercareStatus || "PENDING")} · simulated</strong><p>${message}</p></div>`;
    }
    if (booking.status !== "CANCELLED") return "";
    return `<div class="booking-aftercare-actions">
      <p>Would you like to make a demo refund or ticket exchange request? A request is recorded for review only.</p>
      <button class="button button-secondary" type="button" data-aftercare-type="REFUND" data-booking-id="${escapeHtml(booking.id)}">Request demo refund</button>
      <button class="button button-secondary" type="button" data-aftercare-type="EXCHANGE" data-booking-id="${escapeHtml(booking.id)}">Request demo ticket exchange</button>
      <p class="booking-action-feedback" data-aftercare-feedback hidden role="status"></p>
    </div>`;
  }

  function bookingTicketPrice(booking) {
    const direct = booking.ticketPrice == null || booking.ticketPrice === "" ? NaN : Number(booking.ticketPrice);
    if (Number.isFinite(direct) && direct >= 0) return direct;
    const total = Number(booking.totalPrice);
    const seatCount = Array.isArray(booking.seatLabels) ? booking.seatLabels.length : 0;
    if (Number.isFinite(total) && total >= 0 && seatCount > 0) return total / seatCount;
    return null;
  }

  function bookingCard(booking, ticketPage = false) {
    const cancellable = state.authUser && booking.status === "CONFIRMED" &&
      new Date(booking.startTime).getTime() > Date.now();
    return `<article class="booking-record">
      <div class="booking-record-heading"><div><span class="eyebrow">${booking.status === "CANCELLED" ? "Cancelled reservation" : "Confirmed reservation"}</span><h2>${escapeHtml(booking.movieTitle)}</h2></div><div class="booking-record-actions"><span class="booking-status ${booking.status === "CANCELLED" ? "is-cancelled" : ""}">${escapeHtml(booking.status)}</span><button class="button button-secondary print-ticket" type="button" data-print-ticket>Print ticket</button></div></div>
      <p class="booking-location">${escapeHtml(booking.cinemaName)} · ${escapeHtml(booking.cinemaAddress || booking.city || "")} · ${escapeHtml(booking.city || "")} · ${escapeHtml(booking.screenName)} · ${escapeHtml(formatShowDate(booking.startTime))} at ${escapeHtml(formatTime(booking.startTime))}</p>
      <div class="booking-seat-list">${booking.seatLabels.map((seat) => `<span>${escapeHtml(seat)}</span>`).join("")}</div>
      <div class="booking-ticket-details"><div class="booking-record-footer"><span>Booking ID <strong>#${escapeHtml(booking.id)}</strong></span><span>Server total <strong>${escapeHtml(Number(booking.totalPrice).toFixed(2))}</strong></span></div>
        <div class="booking-record-footer"><span>Seats subtotal <strong>${escapeHtml(Number(booking.seatSubtotal ?? booking.totalPrice).toFixed(2))}</strong></span><span>Food subtotal <strong>${escapeHtml(Number(booking.foodSubtotal || 0).toFixed(2))}</strong></span></div>
        ${Array.isArray(booking.foodItems) && booking.foodItems.length ? `<div class="booking-record-footer"><span>Food order <strong>${booking.foodItems.map((item) => `${escapeHtml(item.name)} × ${escapeHtml(item.quantity)}`).join(", ")}</strong></span></div>` : ""}
        ${booking.ticketType ? `<div class="booking-record-footer"><span>Ticket type <strong>${escapeHtml(booking.ticketType)}</strong></span></div>` : ""}
        <div class="booking-record-footer"><span>Payment method <strong>${escapeHtml(booking.paymentMethod || "DEMO")}</strong></span><span>Payment status <strong>${booking.paymentStatus === "SIMULATED_SUCCESS" ? "Success (simulated)" : escapeHtml(booking.paymentStatus || "Success")}</strong></span></div>
        <div class="booking-code-panel">${bookingCodeMarkup(booking.bookingReference)}<div><span class="eyebrow">Booking reference</span><p class="booking-reference">${escapeHtml(booking.bookingReference)}</p><p class="booking-code-note">Illustrative only · not scannable</p></div></div>
      </div>
      ${booking.cancellationReason ? `<div class="booking-cancellation-info"><strong>Cancellation reason</strong><p>${escapeHtml(booking.cancellationReason)}</p>${booking.cancelledAt ? `<small>Cancelled ${escapeHtml(formatShowDate(booking.cancelledAt))} at ${escapeHtml(formatTime(booking.cancelledAt))}</small>` : ""}</div>` : ""}
      ${bookingAftercareMarkup(booking)}
      ${!ticketPage && booking.status === "CANCELLED" ? `<div class="booking-actions"><button class="button button-secondary" type="button" data-remove-cancelled="${escapeHtml(booking.id)}">Remove cancelled ticket</button></div>` : ""}
      ${cancellable ? `<div class="booking-actions"><a class="button button-secondary" href="#/bookings/${encodeURIComponent(booking.id)}">Open ticket</a><button class="button button-secondary" type="button" data-show-cancel="${escapeHtml(booking.id)}">Cancel booking</button></div>
        <form class="booking-cancel-form" data-cancel-form="${escapeHtml(booking.id)}" hidden>
          <label>Why are you cancelling?<textarea name="reason" required maxlength="500" rows="3" placeholder="Enter a brief reason (maximum 500 characters)"></textarea></label>
          <p class="booking-action-feedback" data-cancel-feedback hidden role="alert"></p>
          <div class="booking-actions"><button class="button button-primary" type="submit">Confirm cancellation</button><button class="button button-secondary" type="button" data-hide-cancel>Keep booking</button></div>
        </form>` : ticketPage ? "" : `<div class="booking-actions"><a class="button button-secondary" href="#/bookings/${encodeURIComponent(booking.id)}">Open ticket</a></div>`}
    </article>`;
  }

  async function renderBookings(viewId) {
    if (!state.authReady) {
      app.innerHTML = `<section class="bookings-page page-wrap">${statePanel("Checking your bookings", "Loading your CineVerse account.", "")}</section>`;
      return;
    }
    if (state.authUser) {
      await renderCustomerBookings(viewId);
      return;
    }
    app.innerHTML = `<section class="bookings-page page-wrap customer-bookings-page">
      <span class="eyebrow">Your CineVerse</span>
      <article class="bookings-card" aria-labelledby="bookings-login-title">
        <span class="ticket-mark" aria-hidden="true">${icons.ticket}</span>
        <h1 id="bookings-login-title">Log in to view your booking history</h1>
        <p>Your tickets and booking history are private to your account. Log in to continue.</p>
        <a class="button button-primary" href="#/login?next=%23%2Fbookings">Login ${icons.arrow}</a>
      </article>
    </section>`;
  }

  async function renderTicket(bookingId, viewId) {
    if (!state.authReady) {
      app.innerHTML = `<section class="bookings-page page-wrap">${statePanel("Checking ticket access", "Verifying your CineVerse account.", "")}</section>`;
      return;
    }
    if (!state.authUser) {
      location.hash = `#/login?next=${encodeURIComponent(`#/bookings/${bookingId}`)}`;
      return;
    }
    app.innerHTML = `<section class="bookings-page page-wrap">${statePanel("Loading ticket", "Retrieving this booking from your account.", "")}</section>`;
    try {
      const booking = await api.getBooking(bookingId);
      if (viewId !== state.viewSequence) return;
      app.innerHTML = `<section class="bookings-page page-wrap customer-bookings-page">
        <a class="back-link" href="#/bookings">${icons.arrow} My bookings</a>
        <span class="eyebrow">Your CineVerse ticket</span><h1 class="page-title">Booking #${escapeHtml(booking.id)}</h1>
        <div class="booking-history">${bookingCard(booking, true)}</div>
      </section>`;
    } catch (error) {
      if (viewId !== state.viewSequence) return;
      app.innerHTML = `<section class="bookings-page page-wrap">${statePanel("Ticket unavailable", error.message, "bookings")}</section>`;
    }
  }

  function filteredCustomerBookings() {
    return state.customerBookings.filter((booking) => {
      const showDate = String(booking.startTime || "").slice(0, 10);
      const statusMatches = !state.bookingStatusFilter || booking.status === state.bookingStatusFilter;
      return statusMatches && (!state.bookingDateFilter || showDate === state.bookingDateFilter);
    });
  }

  function renderCustomerBookingResults() {
    const target = document.getElementById("customer-booking-results");
    const count = document.getElementById("customer-booking-count");
    if (!target || !count) return;
    const bookings = filteredCustomerBookings();
    count.textContent = `${bookings.length} ${bookings.length === 1 ? "booking" : "bookings"}`;
    target.innerHTML = bookings.length
      ? bookings.map(bookingCard).join("")
      : `<div class="bookings-card"><h2>No bookings match these filters.</h2><p>Try another show date or status, or clear filters to view your full booking history.</p></div>`;
  }

  async function submitBookingCancellation(form) {
    const bookingId = form.dataset.cancelForm;
    const reason = String(new FormData(form).get("reason") || "").trim();
    const button = form.querySelector('[type="submit"]');
    const feedback = form.querySelector("[data-cancel-feedback]");
    button.disabled = true;
    button.textContent = "Cancelling…";
    if (feedback) feedback.hidden = true;
    try {
      const booking = await api.cancelBooking(bookingId, reason);
      state.customerBookings = state.customerBookings.map((entry) => String(entry.id) === String(booking.id) ? booking : entry);
      if (location.hash.startsWith(`#/bookings/${bookingId}`)) {
        await renderTicket(bookingId, state.viewSequence);
      } else {
        renderCustomerBookingResults();
      }
    } catch (error) {
      if (feedback) {
        feedback.textContent = error.message;
        feedback.hidden = false;
      }
      button.disabled = false;
      button.textContent = "Confirm cancellation";
    }
  }

  async function submitAftercareRequest(button) {
    const bookingId = button.dataset.bookingId;
    const feedback = button.closest(".booking-aftercare-actions")?.querySelector("[data-aftercare-feedback]");
    const buttons = [...(button.closest(".booking-aftercare-actions")?.querySelectorAll("[data-aftercare-type]") || [])];
    buttons.forEach((item) => { item.disabled = true; });
    try {
      const booking = await api.requestBookingAftercare(bookingId, button.dataset.aftercareType);
      state.customerBookings = state.customerBookings.map((entry) => String(entry.id) === String(booking.id) ? booking : entry);
      if (location.hash.startsWith(`#/bookings/${bookingId}`)) await renderTicket(bookingId, state.viewSequence);
      else renderCustomerBookingResults();
    } catch (error) {
      if (feedback) {
        feedback.textContent = error.message;
        feedback.hidden = false;
      }
    }
    buttons.forEach((item) => { item.disabled = false; });
  }

  async function removeCancelledBooking(button) {
    const bookingId = button.dataset.removeCancelled;
    if (!window.confirm("Remove this cancelled ticket from your booking history? This cannot be undone.")) return;
    button.disabled = true;
    button.textContent = "Removing…";
    try {
      await api.removeCancelledBooking(bookingId);
      state.customerBookings = state.customerBookings.filter((booking) => String(booking.id) !== bookingId);
      renderCustomerBookingResults();
    } catch (error) {
      button.disabled = false;
      button.textContent = "Remove cancelled ticket";
      const feedback = document.createElement("p");
      feedback.className = "booking-action-feedback";
      feedback.setAttribute("role", "alert");
      feedback.textContent = error.message;
      button.parentElement?.append(feedback);
    }
  }

  function customerBookingReportCsv(bookings) {
    const cell = (value) => {
      const text = String(value == null ? "" : value);
      const safe = /^[=+\-@\t\r]/.test(text) ? `'${text}` : text;
      return `"${safe.replace(/"/g, '""')}"`;
    };
      const rows = [
        ["Booking ID", "Movie", "Cinema", "Screen", "Show date", "Show time", "Seats", "Status", "Total", "Cancellation reason", "Follow-up request", "Request status"],
      ...bookings.map((booking) => [
        booking.id,
        booking.movieTitle,
        booking.cinemaName,
        booking.screenName,
        formatShowDate(booking.startTime),
        formatTime(booking.startTime),
        (booking.seatLabels || []).join(", "),
        booking.status,
        Number(booking.totalPrice).toFixed(2),
        booking.cancellationReason || "",
        booking.aftercareType || "",
        booking.aftercareStatus || ""
      ])
    ];
    return rows.map((row) => row.map(cell).join(",")).join("\r\n");
  }

  async function renderCustomerBookings(viewId) {
    state.bookingDateFilter = "";
    state.bookingStatusFilter = "";
    const user = state.authUser;
    app.innerHTML = `<section class="bookings-page page-wrap customer-bookings-page">
      <span class="eyebrow">Your CineVerse</span>
      <h1 class="page-title">Your bookings.</h1>
      <p class="bookings-intro">Tickets reserved for ${escapeHtml(user.displayName)}. This report contains only bookings linked to your signed-in account.</p>
      <section class="booking-report-panel" aria-label="Personal booking report">
        <div class="booking-report-heading"><div><span class="eyebrow">Personal report</span><h2>Booking history</h2></div><div class="booking-report-actions"><button class="button button-secondary" type="button" data-print-report>Print report</button><button class="button button-primary" type="button" data-export-bookings>Export CSV</button></div></div>
        <div class="booking-report-controls">
          <label class="select-wrap"><select class="genre-select" id="booking-date-filter" aria-label="Filter bookings by show date"><option value="">All show dates</option></select></label>
          <label class="select-wrap"><select class="genre-select" id="booking-status-filter" aria-label="Filter bookings by status"><option value="">All statuses</option></select></label>
          <button class="button button-secondary" type="button" data-clear-booking-filters>Clear filters</button>
          <span class="result-count" id="customer-booking-count" aria-live="polite">Loading bookings…</span>
        </div>
        <div class="booking-history" id="customer-booking-results">${statePanel("Loading your tickets", "Retrieving bookings associated with your account.", "")}</div>
      </section>
      <p class="booking-storage-note">Demo data is stored in the running in-memory database and is cleared when the server restarts.</p>
    </section>`;
    try {
      const bookings = await api.getMyBookings();
      if (viewId !== state.viewSequence) return;
      state.customerBookings = Array.isArray(bookings) ? bookings : [];
      const dates = [...new Set(state.customerBookings.map((booking) => String(booking.startTime || "").slice(0, 10))
        .filter((date) => /^\d{4}-\d{2}-\d{2}$/.test(date)))].sort();
      const dateFilter = document.getElementById("booking-date-filter");
      dateFilter.innerHTML += dates.map((date) => {
        const label = new Intl.DateTimeFormat(undefined, { weekday: "short", year: "numeric", month: "short", day: "numeric" })
          .format(new Date(`${date}T12:00:00`));
        return `<option value="${escapeHtml(date)}">${escapeHtml(label)}</option>`;
      }).join("");
      document.getElementById("booking-status-filter").innerHTML += [...new Set(state.customerBookings
        .map((booking) => booking.status).filter((status) => typeof status === "string"))]
        .sort()
        .map((status) => `<option value="${escapeHtml(status)}">${escapeHtml(status.charAt(0) + status.slice(1).toLowerCase())}</option>`)
        .join("");
      dateFilter.addEventListener("change", () => {
        state.bookingDateFilter = dateFilter.value;
        renderCustomerBookingResults();
      });
      document.getElementById("booking-status-filter").addEventListener("change", (event) => {
        state.bookingStatusFilter = event.target.value;
        renderCustomerBookingResults();
      });
      renderCustomerBookingResults();
    } catch (error) {
      if (viewId !== state.viewSequence) return;
      document.getElementById("customer-booking-count").textContent = "";
      document.getElementById("customer-booking-results").innerHTML = statePanel(
        "Your bookings couldn't load",
        error.message,
        "bookings");
    }
  }

  function renderAuthPage(mode, next = "") {
    const signup = mode === "signup";
    const adminLogin = mode === "admin-login";
    const bookingNext = /^#\/book\/(?:local|tmdb)\/\d+$/.test(next) || /^#\/book\/curated\/[a-z-]+$/.test(next);
    const bookingCity = bookingNext ? state.selectedCity : "";
    const title = signup ? "Create your account." : adminLogin ? "Administrator sign in." : "Welcome back.";
    const intro = signup
      ? "Save your CineVerse profile and keep your movie nights close."
      : adminLogin
        ? "Use your separately configured administrator account to access booking reports."
        : "Sign in to view your profile and pick up where movie night left off.";
    app.innerHTML = `<section class="auth-page page-wrap" aria-labelledby="auth-title">
      <div class="auth-card">
        <a class="auth-back-link" href="#/">${icons.arrow} Back to CineVerse</a>
        <span class="eyebrow">${signup ? "Join CineVerse" : adminLogin ? "Admin portal" : "Your account"}</span>
        <h1 class="page-title" id="auth-title">${title}</h1>
        <p class="auth-intro">${bookingCity
          ? `Selected city: ${escapeHtml(bookingCity)}. Sign in to see showtimes there; your city selection will be kept.`
          : intro}</p>
        <form class="auth-form" data-auth-form="${signup ? "signup" : adminLogin ? "admin-login" : "login"}">
          ${signup ? `<label for="auth-name">Name</label><input id="auth-name" name="displayName" type="text" autocomplete="name" maxlength="80" required placeholder="Your name">` : ""}
          <label for="auth-email">Email</label>
          <input id="auth-email" name="email" type="email" autocomplete="email" maxlength="254" required placeholder="you@example.com">
          <label for="auth-password">Password</label>
          <input id="auth-password" name="password" type="password" autocomplete="${signup ? "new-password" : "current-password"}" ${signup ? 'minlength="10" maxlength="72"' : 'maxlength="72"'} required placeholder="${signup ? "At least 10 characters" : "Your password"}">
          ${!signup ? `<label for="auth-admin-id">Admin ID <span>(optional)</span></label>
          <input id="auth-admin-id" name="adminId" type="password" autocomplete="off" maxlength="128" placeholder="Enter this only if you are an admin">
          <p class="auth-password-hint">Leave blank for customer login. Admins must enter their configured Admin ID.</p>` : ""}
          ${signup ? `<p class="auth-password-hint">Use at least 10 characters. Passwords are securely hashed before storage.</p>` : ""}
          <p class="auth-feedback" data-auth-feedback role="alert" hidden></p>
          <button class="button button-primary auth-submit" type="submit">${signup ? "Create account" : adminLogin ? "Admin log in" : "Log in"} ${icons.arrow}</button>
        </form>
        <p class="auth-switch">${adminLogin ? "Customer account?" : signup ? "Already have an account?" : "New to CineVerse?"} <a href="#/${adminLogin ? "login" : signup ? "login" : "signup"}${next ? `?next=${encodeURIComponent(next)}` : ""}">${adminLogin ? "Customer login" : signup ? "Log in" : "Create an account"}</a></p>
        <p class="auth-privacy">${adminLogin ? "Administrator access is verified by the server." : "Demo account only. No payment details are collected."}</p>
      </div>
    </section>`;
  }

  function renderProfile(viewId) {
    if (!state.authReady) {
      app.innerHTML = `<div class="page-wrap detail-body">${statePanel("Loading your profile", "Checking your CineVerse account.", "")}</div>`;
      return;
    }
    if (!state.authUser) {
      if (state.authError) {
        app.innerHTML = `<section class="profile-page page-wrap">${statePanel("Profile unavailable", state.authError, "profile")}</section>`;
        return;
      }
      location.hash = "#/login";
      return;
    }
    const user = state.authUser;
    app.innerHTML = `<section class="profile-page page-wrap" aria-labelledby="profile-title">
      <span class="eyebrow">Your CineVerse</span>
      <h1 class="page-title" id="profile-title">Your profile.</h1>
      <p class="profile-intro">Your account details, ready for your next movie night.</p>
      <article class="profile-card">
        <div class="profile-avatar" aria-hidden="true">${escapeHtml(user.displayName.trim().slice(0, 1).toUpperCase())}</div>
        <div class="profile-details">
          <span class="eyebrow">Member profile</span>
          <h2>${escapeHtml(user.displayName)}</h2>
          <p>${escapeHtml(user.email)}</p>
          <p class="profile-joined">Member since ${escapeHtml(new Intl.DateTimeFormat(undefined, { dateStyle: "long" }).format(new Date(user.createdAt)))}</p>
        </div>
        <a class="button button-secondary" href="#/bookings">My bookings ${icons.arrow}</a>
      </article>
      ${state.authError ? `<p class="auth-feedback" role="alert">${escapeHtml(state.authError)}</p>` : ""}
    </section>`;
    if (viewId !== state.viewSequence) return;
  }

  function adminReportCsv(bookings) {
    const cell = (value) => {
      const text = String(value == null ? "" : value);
      const safe = /^[=+\-@\t\r]/.test(text) ? `'${text}` : text;
      return `"${safe.replace(/"/g, '""')}"`;
    };
    const rows = [
      ["Booking ID", "Reference", "Customer", "Email", "Movie", "Cinema", "City", "Screen", "Show date", "Show time", "Seats", "Total", "Payment method", "Payment status", "Status", "Cancellation reason", "Follow-up request", "Request status"],
      ...bookings.map((booking) => [
        booking.id,
        booking.bookingReference,
        booking.customerName,
        booking.customerEmail,
        booking.movieTitle,
        booking.cinemaName,
        booking.city,
        booking.screenName,
        formatShowDate(booking.showStartTime),
        formatTime(booking.showStartTime),
        (booking.seatLabels || []).join(", "),
        Number(booking.totalPrice).toFixed(2),
        booking.paymentMethod,
        booking.paymentStatus,
        booking.status,
        booking.cancellationReason || "",
        booking.aftercareType || "",
        booking.aftercareStatus || ""
      ])
    ];
    return rows.map((row) => row.map(cell).join(",")).join("\r\n");
  }

  function adminBreakdownMarkup(title, values) {
    const rows = Object.entries(values || {}).sort((left, right) => right[1] - left[1] || left[0].localeCompare(right[0]));
    if (!rows.length) {
      return `<section class="admin-breakdown"><h3>${escapeHtml(title)}</h3><p class="admin-breakdown-empty">No report data yet.</p></section>`;
    }
    const maximum = Math.max(1, ...rows.map(([, value]) => Number(value) || 0));
    return `<section class="admin-breakdown"><h3>${escapeHtml(title)}</h3><div class="admin-breakdown-list">${rows.map(([label, count]) => {
      const amount = Math.max(0, Number(count) || 0);
      const width = Math.min(100, amount / maximum * 100);
      return `<div class="admin-breakdown-row"><span title="${escapeHtml(label)}">${escapeHtml(label)}</span><div class="admin-breakdown-bar"><i style="width:${width}%"></i></div><strong>${escapeHtml(amount)}</strong></div>`;
    }).join("")}</div></section>`;
  }

  function adminShowPricingMarkup(shows) {
    const rows = shows.length ? shows.map((show) => `<tr>
      <td><strong>${escapeHtml(show.movieTitle)}</strong><small>${escapeHtml(show.status)}</small></td>
      <td>${escapeHtml(show.city)}<small>${escapeHtml(show.cinemaName)} · ${escapeHtml(show.screenName)}</small></td>
      <td>${escapeHtml(formatShowDate(show.startTime))}<small>${escapeHtml(formatTime(show.startTime))}</small></td>
      <td><form class="admin-price-form" data-admin-price-form="${escapeHtml(show.id)}">
        <label class="visually-hidden" for="admin-price-${escapeHtml(show.id)}">Price per ticket for ${escapeHtml(show.movieTitle)}</label>
        <input class="admin-price-input" id="admin-price-${escapeHtml(show.id)}" name="ticketPrice" type="number" min="0.01" max="999999.99" step="0.01" required value="${escapeHtml(Number(show.ticketPrice).toFixed(2))}">
        <button class="button button-secondary" type="submit">Save price</button>
        <span class="admin-price-feedback" data-admin-price-feedback role="status"></span>
      </form></td>
    </tr>`).join("") : `<tr><td class="admin-empty-row" colspan="4">No screenings are configured yet.</td></tr>`;
    return `<section class="admin-booking-report admin-pricing-report" aria-labelledby="admin-pricing-title">
      <div class="admin-booking-heading"><div><span class="eyebrow">Ticket prices</span><h2 id="admin-pricing-title">Manage screening prices</h2></div></div>
      <p class="admin-data-note">Price is per seat for this screening and applies to new bookings after the change.</p>
      <div class="admin-table-scroll"><table class="admin-booking-table"><thead><tr><th>Movie</th><th>Cinema / screen</th><th>Showtime</th><th>Price per ticket (INR)</th></tr></thead><tbody>${rows}</tbody></table></div>
    </section>`;
  }

  async function renderAdminDashboard(viewId, from = "", to = "") {
    if (!state.authReady) {
      app.innerHTML = `<section class="admin-page page-wrap">${statePanel("Checking admin access", "Verifying your CineVerse account.", "")}</section>`;
      return;
    }
    if (!state.authUser) {
      location.hash = "#/admin/login";
      return;
    }
    if (state.authUser.role !== "ADMIN") {
      app.innerHTML = `<section class="admin-page page-wrap">${statePanel("Admin access required", "Sign in with the separate CineVerse admin account to view reports.", "")}<a class="button button-secondary" href="#/admin/login">Go to admin login</a></section>`;
      return;
    }
    const reportRequest = ++state.adminReportRequest;
    app.innerHTML = `<section class="admin-page page-wrap" aria-labelledby="admin-title">
      <span class="eyebrow">CineVerse management</span>
      <h1 class="page-title" id="admin-title">Admin dashboard.</h1>
      <p class="admin-intro">View booking reports or manage screening prices. Reports and revenue use confirmed demo reservations only.</p>
      <div class="admin-subsection-tabs" role="tablist" aria-label="Admin dashboard sections">
        <button class="admin-subsection-tab" id="admin-reports-tab" type="button" role="tab" aria-selected="true" aria-controls="admin-reports-panel" data-admin-tab="reports">View reports</button>
        <button class="admin-subsection-tab" id="admin-pricing-tab" type="button" role="tab" aria-selected="false" aria-controls="admin-pricing-panel" data-admin-tab="pricing">Change prices</button>
      </div>
      <section id="admin-reports-panel" class="admin-subsection-panel" role="tabpanel" aria-labelledby="admin-reports-tab">
        <form class="admin-report-filters" id="admin-report-filters">
          <div class="admin-filter-date-fields">
            <label>Show date from<input type="date" name="from" value="${escapeHtml(from)}"></label>
            <label>Show date to<input type="date" name="to" value="${escapeHtml(to)}"></label>
          </div>
          <div class="admin-filter-actions">
            <button class="button button-primary" type="submit">Apply dates</button>
            <button class="button button-secondary" type="button" data-admin-clear>Clear dates</button>
          </div>
          <p class="admin-filter-error" data-admin-filter-error role="alert" hidden></p>
          <p class="admin-filter-note">Choose either date or both. The selected dates include the full show days.</p>
        </form>
        <div id="admin-dashboard-results" class="admin-dashboard-results">${statePanel("Loading reports", "Retrieving booking and catalog summaries.", "")}</div>
      </section>
      <section id="admin-pricing-panel" class="admin-subsection-panel" role="tabpanel" aria-labelledby="admin-pricing-tab" hidden>
        <div id="admin-pricing-results" class="admin-dashboard-results">${statePanel("Loading prices", "Retrieving screening prices.", "")}</div>
      </section>
    </section>`;
    const results = document.getElementById("admin-dashboard-results");
    const pricingResults = document.getElementById("admin-pricing-results");
    app.querySelectorAll("[data-admin-tab]").forEach((tab) => {
      tab.addEventListener("click", () => {
        const selected = tab.dataset.adminTab;
        app.querySelectorAll("[data-admin-tab]").forEach((candidate) => {
          candidate.setAttribute("aria-selected", String(candidate === tab));
        });
        document.getElementById("admin-reports-panel").hidden = selected !== "reports";
        document.getElementById("admin-pricing-panel").hidden = selected !== "pricing";
      });
    });
    app.querySelector(".admin-subsection-tabs").addEventListener("keydown", (event) => {
      if (!["ArrowLeft", "ArrowRight", "Home", "End"].includes(event.key)) return;
      event.preventDefault();
      const tabs = [...app.querySelectorAll("[data-admin-tab]")];
      const currentIndex = tabs.indexOf(document.activeElement);
      const nextIndex = event.key === "Home" ? 0
        : event.key === "End" ? tabs.length - 1
          : (currentIndex + (event.key === "ArrowRight" ? 1 : -1) + tabs.length) % tabs.length;
      tabs[nextIndex].focus();
      tabs[nextIndex].click();
    });
    try {
      const [data, shows] = await Promise.all([
        api.getAdminDashboard(from, to),
        api.getAdminShows()
      ]);
      if (viewId !== state.viewSequence || reportRequest !== state.adminReportRequest) return;
      const bookings = Array.isArray(data.bookings) ? data.bookings : [];
      const money = new Intl.NumberFormat(undefined, { style: "currency", currency: "INR" });
      const bookingRows = bookings.length ? bookings.map((booking) => {
        const searchText = [
          booking.id, booking.bookingReference, booking.movieTitle, booking.city,
          booking.cinemaName, booking.screenName, booking.showStartTime,
          ...(booking.seatLabels || []), booking.paymentMethod, booking.paymentStatus,
          booking.status, booking.aftercareType, booking.aftercareStatus
        ].join(" ").toLowerCase();
        return `<tr data-admin-row data-status="${escapeHtml(booking.status)}" data-search="${escapeHtml(searchText)}">
        <td>#${escapeHtml(booking.id)}</td>
        <td><strong>${escapeHtml(booking.movieTitle)}</strong><small>${escapeHtml(booking.bookingReference)}</small></td>
        <td>${escapeHtml(booking.city)}</td>
        <td>${escapeHtml(formatShowDate(booking.showStartTime))}<small>${escapeHtml(formatTime(booking.showStartTime))} · ${escapeHtml(booking.cinemaName)} · ${escapeHtml(booking.screenName)}</small></td>
        <td>${escapeHtml((booking.seatLabels || []).join(", "))}<small>${(booking.seatLabels || []).length} seats</small></td>
        <td>${escapeHtml(money.format(Number(booking.totalPrice)))}</td>
        <td>${escapeHtml(booking.paymentMethod || "Unknown")}<small>${escapeHtml(booking.paymentStatus || "Unknown")}</small></td>
        <td>${escapeHtml(booking.status)}<small>${booking.aftercareType ? `${escapeHtml(booking.aftercareType)} · ${escapeHtml(booking.aftercareStatus || "PENDING")} (simulated)` : "No refund/exchange request"}</small></td>
      </tr>`;
      }).join("") : `<tr><td class="admin-empty-row" colspan="8">No bookings match the selected show dates.</td></tr>`;
      results.innerHTML = `<div class="admin-summary-grid">
        <article class="admin-summary-card"><span>Movies</span><strong>${escapeHtml(data.totalMovies)}</strong><small>Local catalog</small></article>
        <article class="admin-summary-card"><span>Cinemas</span><strong>${escapeHtml(data.totalCinemas)}</strong><small>Across the demo</small></article>
        <article class="admin-summary-card"><span>Scheduled shows</span><strong>${escapeHtml(data.totalShows)}</strong><small>Current schedule</small></article>
        <article class="admin-summary-card"><span>Customer accounts</span><strong>${escapeHtml(data.totalCustomers)}</strong><small>Including admin accounts</small></article>
        <article class="admin-summary-card"><span>Bookings in range</span><strong>${escapeHtml(data.totalBookings)}</strong><small>Based on show date</small></article>
        <article class="admin-summary-card"><span>Seats booked</span><strong>${escapeHtml(data.totalSeatsBooked || 0)}</strong><small>Confirmed bookings</small></article>
        <article class="admin-summary-card admin-revenue-card"><span>Demo revenue in range</span><strong>${escapeHtml(money.format(Number(data.totalRevenue)))}</strong><small>Confirmed bookings only</small></article>
      </div>
      <div class="admin-breakdown-grid">
        ${adminBreakdownMarkup("Bookings by movie · all statuses", data.bookingsByMovie)}
        ${adminBreakdownMarkup("Tickets booked by movie · confirmed", data.mostBookedMovies)}
        ${adminBreakdownMarkup("Bookings by city", data.bookingsByCity)}
        ${adminBreakdownMarkup("Show/date bookings", data.bookingsByShow)}
        ${adminBreakdownMarkup("Payment methods", data.bookingsByPaymentMethod)}
        ${adminBreakdownMarkup("Payment statuses", data.bookingsByPaymentStatus)}
        ${adminBreakdownMarkup("Cancellations", data.cancellations)}
        ${adminBreakdownMarkup("Refund/exchange requests", data.refundRequests)}
      </div>
      <section class="admin-booking-report" aria-labelledby="admin-bookings-title">
        <div class="admin-booking-heading"><div><span class="eyebrow">Booking report</span><h2 id="admin-bookings-title">Reservations</h2></div><button class="button button-secondary" type="button" data-export-admin ${bookings.length ? "" : "disabled"}>Export CSV</button></div>
        <div class="admin-table-filters"><label>Search bookings<input id="admin-booking-search" type="search" placeholder="Movie, city, seat or payment"></label><label>Status<select id="admin-booking-status"><option value="">All statuses</option><option value="CONFIRMED">Confirmed</option><option value="CANCELLED">Cancelled</option></select></label></div>
        <div class="admin-table-scroll"><table class="admin-booking-table"><thead><tr><th>ID</th><th>Movie</th><th>City</th><th>Showtime</th><th>Seats</th><th>Amount</th><th>Payment</th><th>Cancellation / refund</th></tr></thead><tbody>${bookingRows}</tbody></table></div>
        <p class="admin-data-note">Demo data is held in the running in-memory H2 database and resets when the app restarts. No real payments are processed.</p>
      </section>`;
      pricingResults.innerHTML = adminShowPricingMarkup(Array.isArray(shows) ? shows : []);
      const form = document.getElementById("admin-report-filters");
      form.addEventListener("submit", (event) => {
        event.preventDefault();
        const startDate = form.elements.from.value;
        const endDate = form.elements.to.value;
        const filterError = form.querySelector("[data-admin-filter-error]");
        if (startDate && endDate && startDate > endDate) {
          filterError.textContent = "The start date must be on or before the end date.";
          filterError.hidden = false;
          return;
        }
        filterError.hidden = true;
        renderAdminDashboard(viewId, startDate, endDate);
      });
      form.querySelector("[data-admin-clear]").addEventListener("click", () => {
        renderAdminDashboard(viewId);
      });
      pricingResults.querySelectorAll("[data-admin-price-form]").forEach((priceForm) => {
        priceForm.addEventListener("submit", async (event) => {
          event.preventDefault();
          if (!priceForm.reportValidity()) return;
          const button = priceForm.querySelector('[type="submit"]');
          const feedback = priceForm.querySelector("[data-admin-price-feedback]");
          const ticketPrice = Number(new FormData(priceForm).get("ticketPrice"));
          button.disabled = true;
          feedback.textContent = "Saving…";
          try {
            const updatedShow = await api.updateAdminShowPrice(priceForm.dataset.adminPriceForm, ticketPrice);
            priceForm.elements.ticketPrice.value = Number(updatedShow.ticketPrice).toFixed(2);
            feedback.textContent = "Price updated.";
          } catch (error) {
            feedback.textContent = `Price wasn't updated: ${error.message}`;
          } finally {
            button.disabled = false;
          }
        });
      });
      const searchInput = document.getElementById("admin-booking-search");
      const statusSelect = document.getElementById("admin-booking-status");
      const filterRows = () => {
        const query = searchInput.value.trim().toLowerCase();
        const status = statusSelect.value;
        results.querySelectorAll("[data-admin-row]").forEach((row) => {
          row.hidden = Boolean((query && !row.dataset.search.includes(query)) || (status && row.dataset.status !== status));
        });
      };
      searchInput.addEventListener("input", filterRows);
      statusSelect.addEventListener("change", filterRows);
      results.querySelector("[data-export-admin]")?.addEventListener("click", () => {
        const csv = adminReportCsv(bookings);
        const url = URL.createObjectURL(new Blob(["\uFEFF", csv], { type: "text/csv;charset=utf-8" }));
        const link = document.createElement("a");
        link.href = url;
        link.download = "cineverse-admin-bookings.csv";
        document.body.append(link);
        link.click();
        link.remove();
        URL.revokeObjectURL(url);
      });
    } catch (error) {
      if (viewId !== state.viewSequence || reportRequest !== state.adminReportRequest) return;
      results.innerHTML = statePanel(
        error.status === 403 ? "Admin access required" : "Dashboard unavailable",
        error.message,
        "");
    }
  }

  function setSeatButtonState(button, status) {
    button.classList.toggle("available", status === "available");
    button.classList.toggle("selected", status === "selected");
    button.classList.toggle("unavailable", status === "booked");
    button.disabled = status === "booked";
    button.setAttribute("aria-pressed", String(status === "selected"));
    button.setAttribute("aria-label", `Seat ${button.dataset.seatLabel}, ${status}`);
  }

  function seatPrice(label) {
    const show = state.currentBookingShow;
    const rowNumber = state.seatRowsByLabel.get(label);
    const multiplier = rowNumber <= 2 ? 1.25 : 1;
    return Math.round((Number(show?.ticketPrice || 0) * multiplier + Number.EPSILON) * 100) / 100;
  }

  function selectedSeatSubtotal() {
    return [...state.selectedSeatLabels].reduce((total, label) => total + seatPrice(label), 0);
  }

  function selectedFoodOrders() {
    return [...state.foodQuantities.entries()]
      .filter(([, quantity]) => quantity > 0)
      .map(([itemId, quantity]) => ({ itemId, quantity }));
  }

  function selectedFoodSubtotal() {
    return selectedFoodOrders().reduce((total, order) => {
      const item = state.foodMenu.find((menuItem) => menuItem.itemId === order.itemId);
      return total + Number(item?.unitPrice || 0) * order.quantity;
    }, 0);
  }

  function seatSelectionMarkup(show, seats, foodMenu) {
    const rows = new Map();
    seats.forEach((seat) => {
      if (!rows.has(seat.rowNumber)) rows.set(seat.rowNumber, []);
      rows.get(seat.rowNumber).push(seat);
    });
    const canBook = show.status === "SCHEDULED" && new Date(show.startTime) > new Date();
    const rowMarkup = [...rows.entries()].map(([, rowSeats]) => {
      const rowLabel = rowSeats[0].label.replace(/\d+$/, "");
      const premium = rowSeats[0].rowNumber <= 2;
      return `<div class="seat-row" role="group" aria-label="Row ${escapeHtml(rowLabel)}">${rowSeats.map((seat) =>
        `<button class="seat-button ${premium ? "premium" : "standard"} ${seat.available ? state.selectedSeatLabels.has(seat.label) ? "selected" : "available" : "unavailable"}" type="button" data-seat-label="${escapeHtml(seat.label)}" aria-pressed="${seat.available && state.selectedSeatLabels.has(seat.label)}" aria-label="Seat ${escapeHtml(seat.label)}, ${premium ? "Premium" : "Standard"}, ${seat.available ? state.selectedSeatLabels.has(seat.label) ? "selected" : "available" : "booked"}" ${seat.available ? "" : "disabled"}>${escapeHtml(seat.label)}</button>`
      ).join("")}</div>`;
    }).join("");
    return `<section class="seat-selection-page page-wrap" aria-labelledby="seat-page-title">
      <a class="back-link" href="#/movies/${encodeURIComponent(show.movieId)}/shows">${icons.arrow} Back to showtimes</a>
      <span class="eyebrow">Choose your seats</span>
      <h1 class="page-title" id="seat-page-title">${escapeHtml(show.movieTitle)}</h1>
      <p class="seat-show-meta">${escapeHtml(show.cinemaName)} · ${escapeHtml(show.city || state.selectedCity || "")} · ${escapeHtml(show.screenName)} · ${escapeHtml(formatShowDate(show.startTime))} at ${escapeHtml(formatTime(show.startTime))}${show.demo ? " · DEMO" : ""}</p>
      <p class="seat-price-note">Price per seat: ${escapeHtml(Number(show.ticketPrice).toFixed(2))}</p>
      <div class="seat-selection-layout">
        <section class="seat-map-panel" aria-label="Seat selection">
          <div class="cinema-screen" aria-hidden="true">SCREEN</div>
          <div class="seat-map" aria-label="Available seats">${rowMarkup}</div>
          <div class="seat-legend"><span><i class="legend-seat premium"></i>Premium · front 2 rows · +25%</span><span><i class="legend-seat standard"></i>Standard · other rows</span><span><i class="legend-seat selected"></i>Selected</span><span><i class="legend-seat unavailable"></i>Booked</span></div>
          <section class="food-order-panel" aria-labelledby="food-order-title">
            <div class="food-order-heading"><div><span class="eyebrow">Before the trailers</span><h2 id="food-order-title">Add snacks</h2></div><span>Added to your bill</span></div>
            <div class="food-menu">${foodMenu.map((item) => `<label class="food-menu-item"><span><strong>${escapeHtml(item.name)}</strong><small>${escapeHtml(item.description)}</small></span><span class="food-menu-price">${escapeHtml(Number(item.unitPrice).toFixed(2))}</span><input type="number" min="0" max="10" step="1" value="${state.foodQuantities.get(item.itemId) || 0}" aria-label="Quantity of ${escapeHtml(item.name)}" data-food-quantity="${escapeHtml(item.itemId)}"></label>`).join("")}</div>
          </section>
        </section>
        <aside class="seat-checkout-panel" aria-label="Booking summary">
          <span class="eyebrow">Your selection</span>
          <label class="ticket-count-label" for="ticket-count">Ticket count
            <select id="ticket-count" class="cinema-select">${Array.from({ length: 10 }, (_, index) =>
              `<option value="${index + 1}" ${state.ticketCount === index + 1 ? "selected" : ""}>${index + 1} ${index === 0 ? "ticket" : "tickets"}</option>`
            ).join("")}</select>
          </label>
          <p class="selected-seat-summary" id="selected-seat-summary">No seats selected</p>
          <p class="food-selection-summary" id="food-selection-summary">No snacks added</p>
          <p class="booking-feedback" id="booking-feedback" role="alert" ${state.bookingNotice ? "" : "hidden"}>${escapeHtml(state.bookingNotice)}</p>
          <p class="selected-total"><span>Seats</span><strong id="booking-seat-total">0.00</strong></p>
          <p class="selected-total"><span>Food</span><strong id="booking-food-total">0.00</strong></p>
          <p class="selected-total booking-grand-total"><span>Total</span><strong id="booking-total">0.00</strong></p>
          <button class="button button-primary booking-submit" type="button" data-confirm-booking disabled>Select at least one seat</button>
          ${canBook ? "" : `<p class="booking-feedback">This show is no longer accepting reservations.</p>`}
        </aside>
      </div>
    </section>`;
  }

  async function renderSeatSelection(showId, viewId) {
    const keepSelection = String(state.currentBookingShow?.id) === String(showId);
    if (!keepSelection) {
      state.selectedSeatLabels = new Set();
      state.ticketCount = 1;
      state.foodQuantities = new Map();
    }
    app.innerHTML = `<div class="page-wrap detail-body">${statePanel("Loading seat map", "Checking this show's seat availability.", "")}</div>`;
    try {
      const [show, seats, foodMenu] = await Promise.all([api.getShow(showId), api.getSeatsForShow(showId), api.getFoodMenu()]);
      if (viewId !== state.viewSequence) return;
      const availableLabels = new Set(seats.filter((seat) => seat.available).map((seat) => seat.label));
      state.selectedSeatLabels.forEach((label) => {
        if (!availableLabels.has(label)) state.selectedSeatLabels.delete(label);
      });
      state.currentBookingShow = show;
      state.seatRowsByLabel = new Map(seats.map((seat) => [seat.label, seat.rowNumber]));
      state.foodMenu = Array.isArray(foodMenu) ? foodMenu : [];
      app.innerHTML = seatSelectionMarkup(show, seats, state.foodMenu);
      state.bookingNotice = "";
      updateBookingSelection();
      if (show.status !== "SCHEDULED" || new Date(show.startTime) <= new Date()) {
        const button = app.querySelector("[data-confirm-booking]");
        if (button) button.disabled = true;
      }
    } catch (error) {
      if (viewId !== state.viewSequence) return;
      app.innerHTML = `<div class="page-wrap" style="padding-top:58px">${statePanel("Seat map unavailable", error.message, `seats:${showId}`)}</div>`;
    }
  }

  function updateBookingSelection() {
    const show = state.currentBookingShow;
    if (!show) return;
    const count = state.selectedSeatLabels.size;
    const seatSubtotal = selectedSeatSubtotal();
    const foodSubtotal = selectedFoodSubtotal();
    const total = (seatSubtotal + foodSubtotal).toFixed(2);
    const summary = document.getElementById("selected-seat-summary");
    const foodSummary = document.getElementById("food-selection-summary");
    const seatTotalElement = document.getElementById("booking-seat-total");
    const foodTotalElement = document.getElementById("booking-food-total");
    const totalElement = document.getElementById("booking-total");
    const button = app.querySelector("[data-confirm-booking]");
    if (summary) summary.textContent = count ? `${[...state.selectedSeatLabels].sort().join(", ")} · ${count} of ${state.ticketCount} tickets`
      : `Select ${state.ticketCount} ${state.ticketCount === 1 ? "seat" : "seats"}`;
    if (foodSummary) foodSummary.textContent = selectedFoodOrders().length
      ? selectedFoodOrders().map((order) => `${state.foodMenu.find((item) => item.itemId === order.itemId)?.name || order.itemId} × ${order.quantity}`).join(", ")
      : "No snacks added";
    if (seatTotalElement) seatTotalElement.textContent = seatSubtotal.toFixed(2);
    if (foodTotalElement) foodTotalElement.textContent = foodSubtotal.toFixed(2);
    if (totalElement) totalElement.textContent = total;
    if (button && new Date(show.startTime) > new Date() && show.status === "SCHEDULED") {
      button.disabled = count !== state.ticketCount;
      button.textContent = count === state.ticketCount ? `Continue to checkout · ${total}` : `Select ${state.ticketCount - count} more ${state.ticketCount - count === 1 ? "seat" : "seats"}`;
    }
  }

  async function confirmBooking(button) {
    const show = state.currentBookingShow;
    const labels = [...state.selectedSeatLabels];
    if (!show || labels.length !== state.ticketCount) return;
    location.hash = "#/checkout";
  }

  function renderCheckout() {
    const show = state.currentBookingShow;
    const labels = [...state.selectedSeatLabels].sort();
    if (!show || !labels.length || labels.length !== state.ticketCount) {
      location.hash = "#/movies";
      return;
    }
    const seatSubtotal = selectedSeatSubtotal();
    const foodSubtotal = selectedFoodSubtotal();
    const total = (seatSubtotal + foodSubtotal).toFixed(2);
    const foodItems = selectedFoodOrders();
    const paymentMethods = [
      ["upi", "UPI", "U"],
      ["card", "Card", "C"],
      ["netbanking", "Net banking", "N"],
      ["wallet", "Wallet", "W"]
    ];
    const paymentDialogs = paymentMethods.map(([method, label]) => {
      const fields = method === "upi"
        ? `<label for="demo-upi-handle">Fictional UPI handle</label><input id="demo-upi-handle" name="upiHandle" type="text" value="demo.user@cineverse.test" pattern="[A-Za-z0-9._-]+@cineverse\\.test" autocomplete="off" required>`
        : method === "card"
          ? `<label for="demo-cardholder">Fictional cardholder name</label><input id="demo-cardholder" name="cardholder" type="text" value="Demo Customer" pattern="[A-Za-z ]{2,40}" autocomplete="off" required><label for="demo-card-profile">Card profile</label><select id="demo-card-profile" name="cardProfile" required><option value="4242">CineVerse test card ·•• 4242</option></select>`
          : method === "netbanking"
            ? `<label for="demo-bank-profile">Bank profile</label><select id="demo-bank-profile" name="bankProfile" required><option value="sandbox">CineVerse test bank</option></select><label for="demo-bank-reference">Fictional reference</label><input id="demo-bank-reference" name="bankReference" type="text" value="DEMO-ACCOUNT-01" pattern="DEMO-[A-Z0-9-]{4,24}" autocomplete="off" required>`
            : `<label for="demo-wallet-profile">Wallet profile</label><select id="demo-wallet-profile" name="walletProfile" required><option value="cineverse">CineVerse test wallet</option></select><label for="demo-wallet-alias">Fictional wallet alias</label><input id="demo-wallet-alias" name="walletAlias" type="text" value="demo-wallet-01" pattern="demo-wallet-[a-z0-9-]{1,20}" autocomplete="off" required>`;
      return `<dialog class="checkout-payment-dialog" id="payment-dialog-${method}" aria-labelledby="payment-title-${method}">
        <form class="checkout-method-panel" data-demo-checkout data-payment-method="${method}">
          <h2 id="payment-title-${method}">${label} · demo payment</h2>
          <p class="checkout-demo-warning">Fictional demo only. Never enter real financial details.</p>
          <div class="demo-payment-details">${fields}</div>
          <label class="checkout-outcome-label" for="payment-outcome-${method}">Simulated outcome</label>
          <select id="payment-outcome-${method}" name="outcome" required><option value="success">Success</option><option value="failure">Failure</option></select>
          <p class="checkout-feedback" role="status" hidden></p>
          <div class="checkout-actions">
            <button class="button button-primary" type="submit">Pay Now ${icons.arrow}</button>
            <button class="button button-secondary" type="button" data-payment-back="${method}">Change method</button>
          </div>
        </form>
      </dialog>`;
    }).join("");
    app.innerHTML = `<section class="checkout-page page-wrap" aria-labelledby="checkout-title">
      <a class="back-link" href="#/shows/${encodeURIComponent(show.id)}/seats">${icons.arrow} Back to seat selection</a>
      <span class="eyebrow">Payment</span>
      <h1 class="page-title" id="checkout-title">Review your movie night.</h1>
      <p class="checkout-intro">Choose a payment method to continue. Demo payment only.</p>
      <div class="checkout-layout">
        <div class="checkout-method-panel">
          <h2>Select a payment method</h2>
          <div class="checkout-methods">${paymentMethods.map(([method, label, icon]) => `<button class="checkout-method" type="button" data-open-payment="${method}"><span class="checkout-method-icon">${icon}</span><strong>${label}</strong></button>`).join("")}</div>
        </div>
        <aside class="checkout-summary" aria-label="Booking summary">
          <span class="eyebrow">Your reservation</span>
          <h2>${escapeHtml(show.movieTitle)}</h2>
          <p>${escapeHtml(show.cinemaName)} · ${escapeHtml(show.city || state.selectedCity || "")} · ${escapeHtml(show.screenName)}</p>
          <p>${escapeHtml(formatShowDate(show.startTime))} at ${escapeHtml(formatTime(show.startTime))}</p>
          <div class="checkout-seat-list">${labels.map((label) => `<span>${escapeHtml(label)}</span>`).join("")}</div>
          <div class="checkout-price-lines"><div><span>Seats${labels.some((label) => state.seatRowsByLabel.get(label) <= 2) ? " (includes Premium)" : ""}</span><strong>${seatSubtotal.toFixed(2)}</strong></div>
            <div><span>Food</span><strong>${foodSubtotal.toFixed(2)}</strong></div>
            ${foodItems.length ? `<ul>${foodItems.map((order) => {
              const item = state.foodMenu.find((menuItem) => menuItem.itemId === order.itemId);
              return `<li>${escapeHtml(item?.name || order.itemId)} × ${order.quantity}<span>${(Number(item?.unitPrice || 0) * order.quantity).toFixed(2)}</span></li>`;
            }).join("")}</ul>` : ""}
          </div>
          <div class="checkout-total"><span>Total due</span><strong>${total}</strong></div>
        </aside>
      </div>
      ${paymentDialogs}
    </section>`;
  }

  async function completeDemoCheckout(outcome, button) {
    const show = state.currentBookingShow;
    const labels = [...state.selectedSeatLabels];
    const dialog = document.getElementById(`payment-dialog-${state.checkoutMethod}`);
    const form = dialog?.querySelector("[data-demo-checkout]");
    const feedback = form?.querySelector(".checkout-feedback");
    if (state.checkoutSubmitting) return;
    if (!show || !labels.length || labels.length !== state.ticketCount) {
      location.hash = "#/movies";
      return;
    }
    state.checkoutSubmitting = true;
    if (feedback) {
      feedback.textContent = "Processing payment…";
      feedback.hidden = false;
      feedback.classList.remove("is-error");
    }
    button.disabled = true;
    button.textContent = "Processing…";
    await new Promise((resolve) => window.setTimeout(resolve, 650));
    if (outcome === "failure") {
      state.checkoutSubmitting = false;
      if (feedback) {
        feedback.textContent = "Payment failed. No booking was created or seats reserved. Try again or choose another method.";
        feedback.hidden = false;
        feedback.classList.add("is-error");
      }
      button.disabled = false;
      button.innerHTML = `Pay Now ${icons.arrow}`;
      return;
    }
    if (feedback) feedback.textContent = "Payment successful. Saving your ticket…";
    try {
      const booking = await api.createBooking(show.id, labels, state.checkoutMethod, selectedFoodOrders());
      saveBooking(booking);
      dialog?.close();
      location.hash = `#/bookings/${encodeURIComponent(booking.id)}`;
    } catch (error) {
      if (error.status === 401) {
        state.authUser = null;
        state.authReady = true;
        renderAuthNavigation();
        location.hash = `#/login?next=${encodeURIComponent(`#/shows/${show.id}/seats`)}`;
        return;
      }
      if (feedback) {
        feedback.textContent = error.message;
        feedback.hidden = false;
      }
      button.disabled = false;
      button.innerHTML = `Pay Now ${icons.arrow}`;
      if (error.status === 409) {
        try {
          const seats = await api.getSeatsForShow(show.id);
          const unavailableLabels = seats.filter((seat) => !seat.available && state.selectedSeatLabels.has(seat.label))
            .map((seat) => seat.label);
          unavailableLabels.forEach((label) => state.selectedSeatLabels.delete(label));
          if (unavailableLabels.length) {
            state.bookingNotice = `Seat ${unavailableLabels.join(", ")} was just booked. Please select replacement seats.`;
            location.hash = `#/shows/${encodeURIComponent(show.id)}/seats`;
          }
        } catch (refreshError) {
          if (feedback) feedback.textContent = `${error.message} Seat availability could not be refreshed; return to seat selection to check again.`;
        }
      }
    } finally {
      state.checkoutSubmitting = false;
    }
  }

  function parseRoute() {
    const value = location.hash.replace(/^#/, "") || "/";
    const [path, queryString = ""] = value.split("?");
    const parts = path.split("/").filter(Boolean);
    const query = new URLSearchParams(queryString);
    if (!parts.length) return { name: "home" };
    if (parts[0] === "movies" && parts.length === 1) return { name: "movies" };
    if (parts[0] === "bookings" && parts.length === 1) return { name: "bookings" };
    if (parts[0] === "bookings" && parts[1] && /^\d+$/.test(parts[1])) return { name: "ticket", id: parts[1] };
    if (parts[0] === "admin" && parts[1] === "login") return { name: "adminLogin" };
    if (parts[0] === "admin" && parts.length === 1) return { name: "admin" };
    if (parts[0] === "checkout" && parts.length === 1) return { name: "checkout" };
    if (parts[0] === "login" && parts.length === 1) return { name: "login", next: query.get("next") || "" };
    if (parts[0] === "signup" && parts.length === 1) return { name: "signup", next: query.get("next") || "" };
    if (parts[0] === "profile" && parts.length === 1) return { name: "profile" };
    if (parts[0] === "book" && ["local", "tmdb"].includes(parts[1]) && /^\d+$/.test(parts[2] || "")) {
      return { name: "bookingStart", source: parts[1], id: parts[2] };
    }
    if (parts[0] === "book" && parts[1] === "curated" && /^[a-z-]+$/.test(parts[2] || "")) {
      return { name: "bookingStart", source: "curated", id: parts[2] };
    }
    if (parts[0] === "discover" && !parts[1]) {
      const page = Number(query.get("page")) || 1;
      return {
        name: "discoveryCategory",
        category: query.get("category") || "trending",
        page: Math.max(1, Math.min(500, page))
      };
    }
    if (parts[0] === "discover" && parts[1]) return { name: "discoveryDetail", id: parts[1] };
    if (parts[0] === "shows" && parts[1] && parts[2] === "seats") return { name: "seats", id: parts[1] };
    if (parts[0] === "movies" && parts[1] && parts[2] === "shows") return { name: "shows", id: parts[1] };
    if (parts[0] === "movies" && parts[1]) return { name: "detail", id: parts[1] };
    return { name: "home" };
  }

  function safeBookingReturnRoute(value) {
    const route = String(value || "");
    return /^#\/(?:shows\/\d+\/seats|book\/(?:local|tmdb)\/\d+|book\/curated\/[a-z-]+|bookings(?:\/\d+)?|checkout)$/.test(route) ? route : "";
  }

  function routeTo(route) {
    state.viewSequence += 1;
    if (route.name !== "home") stopSpotlightTimer();
    const viewId = state.viewSequence;
    if (["seats", "checkout", "ticket"].includes(route.name) || (route.name === "bookingStart" && !state.authReady)) {
      if (!state.authReady) {
        app.innerHTML = `<div class="page-wrap detail-body">${statePanel("Checking your account", "Please wait while CineVerse verifies your sign-in.", "")}</div>`;
        return;
      }
      if (!state.authUser) {
        const next = route.name === "bookingStart" ? `#/book/${route.source}/${route.id}`
          : route.name === "seats" ? `#/shows/${route.id}/seats`
          : route.name === "ticket" ? `#/bookings/${route.id}` : "#/checkout";
        location.hash = `#/login?next=${encodeURIComponent(next)}`;
        return;
      }
    }
    const activeRoute = ["detail", "shows", "seats", "bookingStart", "discoveryCategory", "discoveryDetail"].includes(route.name)
      ? "movies"
      : route.name;
    setActiveNavigation(activeRoute);
    if (route.name === "home") renderHome(viewId);
    else if (route.name === "movies") renderCatalog(viewId);
    else if (route.name === "detail") renderDetail(route.id, viewId);
    else if (route.name === "discoveryCategory") renderDiscoveryCategory(route.category, route.page, viewId);
    else if (route.name === "discoveryDetail") renderDiscoveryDetail(route.id, viewId);
    else if (route.name === "bookingStart") renderBookingStart(route.source, route.id, viewId);
    else if (route.name === "shows") renderShows(route.id, viewId);
    else if (route.name === "seats") renderSeatSelection(route.id, viewId);
    else if (route.name === "checkout") renderCheckout();
    else if (route.name === "bookings") renderBookings(viewId);
    else if (route.name === "ticket") renderTicket(route.id, viewId);
    else if (route.name === "login" || route.name === "signup") renderAuthPage(route.name, route.next);
    else if (route.name === "adminLogin") renderAuthPage("admin-login");
    else if (route.name === "profile") renderProfile(viewId);
    else if (route.name === "admin") renderAdminDashboard(viewId);
    window.scrollTo({ top: 0, behavior: "smooth" });
  }

  function retryAction(action) {
    if (action === "home") routeTo({ name: "home" });
    else if (action === "catalog") routeTo({ name: "movies" });
    else if (action.startsWith("detail:")) routeTo({ name: "detail", id: action.split(":")[1] });
    else if (action.startsWith("discovery-detail:")) routeTo({ name: "discoveryDetail", id: action.split(":")[1] });
    else if (action.startsWith("discovery-category:")) {
      const [, category, page] = action.split(":");
      routeTo({ name: "discoveryCategory", category, page: Number(page) || 1 });
    }
    else if (action.startsWith("discovery-rail:")) {
      const category = discoveryCategories.find((item) => item.slug === action.split(":")[1]);
      if (category) loadDiscoveryRail(category, state.viewSequence);
    }
    else if (action.startsWith("shows:")) routeTo({ name: "shows", id: action.split(":")[1] });
    else if (action.startsWith("seats:")) routeTo({ name: "seats", id: action.split(":")[1] });
    else if (action === "bookings") routeTo({ name: "bookings" });
    else if (action === "profile") loadAuthState();
    else if (action.startsWith("cinema:")) {
      const [, movieId, cinemaId] = action.split(":");
      const select = document.getElementById("cinema-filter");
      if (select) select.value = cinemaId;
      const movieTitle = document.getElementById("show-movie-title")?.textContent || "this film";
      loadSelectedCinema({ id: movieId, title: movieTitle }, [], cinemaId, state.viewSequence);
    }
  }

  app.addEventListener("click", (event) => {
    const spotlightStep = event.target.closest("[data-spotlight-step]");
    if (spotlightStep) {
      renderSpotlightSlide(state.spotlightIndex + Number(spotlightStep.dataset.spotlightStep), true);
      startSpotlightTimer();
      return;
    }
    const findShowsButton = event.target.closest("[data-find-booking-shows]");
    if (findShowsButton) {
      findBookingShows(findShowsButton);
      return;
    }
    if (event.target.closest("[data-return-to-seats]")) {
      location.hash = `#/shows/${encodeURIComponent(state.currentBookingShow?.id || "")}/seats`;
      return;
    }
    const printTicket = event.target.closest("[data-print-ticket]");
    if (printTicket) {
      window.print();
      return;
    }
    if (event.target.closest("[data-print-report]")) {
      window.print();
      return;
    }
    if (event.target.closest("[data-export-bookings]")) {
      const rows = customerBookingReportCsv(filteredCustomerBookings());
      const url = URL.createObjectURL(new Blob(["\uFEFF", rows], { type: "text/csv;charset=utf-8" }));
      const link = document.createElement("a");
      link.href = url;
      link.download = "cineverse-my-bookings.csv";
      document.body.append(link);
      link.click();
      link.remove();
      URL.revokeObjectURL(url);
      return;
    }
    if (event.target.closest("[data-clear-booking-filters]")) {
      const date = document.getElementById("booking-date-filter");
      const status = document.getElementById("booking-status-filter");
      if (date) date.value = "";
      if (status) status.value = "";
      state.bookingDateFilter = "";
      state.bookingStatusFilter = "";
      renderCustomerBookingResults();
      return;
    }
    const paymentChoice = event.target.closest("[data-open-payment]");
    if (paymentChoice) {
      state.checkoutMethod = paymentChoice.dataset.openPayment;
      document.getElementById(`payment-dialog-${state.checkoutMethod}`)?.showModal();
      return;
    }
    const changePayment = event.target.closest("[data-payment-back]");
    if (changePayment) {
      document.getElementById(`payment-dialog-${changePayment.dataset.paymentBack}`)?.close();
      app.querySelector("[data-open-payment]")?.focus();
      return;
    }
    const showCancel = event.target.closest("[data-show-cancel]");
    if (showCancel) {
      const form = showCancel.closest(".booking-record")?.querySelector(`[data-cancel-form="${CSS.escape(showCancel.dataset.showCancel)}"]`);
      if (form) {
        form.hidden = false;
        showCancel.hidden = true;
        form.querySelector("textarea")?.focus();
      }
      return;
    }
    if (event.target.closest("[data-hide-cancel]")) {
      const form = event.target.closest("[data-cancel-form]");
      if (form) {
        form.hidden = true;
        form.closest(".booking-record")?.querySelector("[data-show-cancel]")?.removeAttribute("hidden");
      }
      return;
    }
    const aftercareButton = event.target.closest("[data-aftercare-type]");
    if (aftercareButton) {
      submitAftercareRequest(aftercareButton);
      return;
    }
    const removeCancelled = event.target.closest("[data-remove-cancelled]");
    if (removeCancelled) {
      removeCancelledBooking(removeCancelled);
      return;
    }
    const retry = event.target.closest("[data-retry]");
    if (retry) {
      retryAction(retry.dataset.retry);
      return;
    }
    const railArrow = event.target.closest("[data-scroll-rail]");
    if (railArrow) {
      const rail = app.querySelector(`[data-rail-content="${CSS.escape(railArrow.dataset.scrollRail)}"]`);
      if (rail) {
        rail.scrollBy({
          left: Number(railArrow.dataset.direction) * Math.max(240, rail.clientWidth * 0.8),
          behavior: "smooth"
        });
      }
      return;
    }
    const discoveryPage = event.target.closest("[data-discovery-page]");
    if (discoveryPage) {
      location.hash = `#/discover?category=${encodeURIComponent(discoveryPage.dataset.category)}&page=${encodeURIComponent(discoveryPage.dataset.discoveryPage)}`;
      return;
    }
    const showtime = event.target.closest("[data-show-id]");
    if (showtime) {
      location.hash = `#/shows/${encodeURIComponent(showtime.dataset.showId)}/seats`;
      return;
    }
    const seat = event.target.closest("[data-seat-label]");
    if (seat && !seat.disabled) {
      const label = seat.dataset.seatLabel;
      if (state.selectedSeatLabels.has(label)) {
        state.selectedSeatLabels.delete(label);
        setSeatButtonState(seat, "available");
      } else if (state.selectedSeatLabels.size < state.ticketCount) {
        state.selectedSeatLabels.add(label);
        setSeatButtonState(seat, "selected");
      } else {
        const feedback = document.getElementById("booking-feedback");
        if (feedback) {
          feedback.textContent = `Your ticket count is ${state.ticketCount}. Increase it before selecting more seats.`;
          feedback.hidden = false;
        }
      }
      updateBookingSelection();
      return;
    }
    const confirmButton = event.target.closest("[data-confirm-booking]");
    if (confirmButton && !confirmButton.disabled) {
      confirmBooking(confirmButton);
    }
  });

  app.addEventListener("change", (event) => {
    const foodQuantity = event.target.closest("[data-food-quantity]");
    if (foodQuantity) {
      const quantity = Math.min(10, Math.max(0, Number(foodQuantity.value) || 0));
      foodQuantity.value = String(quantity);
      state.foodQuantities.set(foodQuantity.dataset.foodQuantity, quantity);
      updateBookingSelection();
      return;
    }
    const countSelect = event.target.closest("#ticket-count");
    if (countSelect) {
      const selectedCount = state.selectedSeatLabels.size;
      const requestedCount = Number(countSelect.value);
      if (requestedCount < selectedCount) {
        [...state.selectedSeatLabels].slice(requestedCount).forEach((label) => {
          state.selectedSeatLabels.delete(label);
          const seatButton = app.querySelector(`[data-seat-label="${CSS.escape(label)}"]`);
          if (seatButton) setSeatButtonState(seatButton, "available");
        });
      }
      state.ticketCount = requestedCount;
      updateBookingSelection();
    }
  });

  app.addEventListener("submit", (event) => {
    const cancelForm = event.target.closest("[data-cancel-form]");
    if (cancelForm) {
      event.preventDefault();
      if (cancelForm.reportValidity()) submitBookingCancellation(cancelForm);
      return;
    }
    const checkoutForm = event.target.closest("[data-demo-checkout]");
    if (checkoutForm) {
      event.preventDefault();
      state.checkoutMethod = checkoutForm.dataset.paymentMethod;
      if (checkoutForm.reportValidity()) {
        completeDemoCheckout(checkoutForm.querySelector('[name="outcome"]').value, checkoutForm.querySelector('[type="submit"]'));
      }
      return;
    }
    const form = event.target.closest("[data-auth-form]");
    if (!form) return;
    event.preventDefault();
    if (!form.reportValidity()) return;
    const feedback = form.querySelector("[data-auth-feedback]");
    const button = form.querySelector('[type="submit"]');
    const signup = form.dataset.authForm === "signup";
    const adminLogin = form.dataset.authForm === "admin-login";
    const formData = new FormData(form);
    const details = {
      email: String(formData.get("email") || "").trim(),
      password: String(formData.get("password") || "")
    };
    if (signup) details.displayName = String(formData.get("displayName") || "").trim();
    if (!signup) details.adminId = String(formData.get("adminId") || "");
    const adminMode = adminLogin || Boolean(details.adminId);
    button.disabled = true;
    button.textContent = signup ? "Creating your account…" : adminMode ? "Verifying admin…" : "Signing in…";
    if (feedback) feedback.hidden = true;
    const submit = signup ? api.registerAccount(details) : adminMode ? api.loginAdmin(details) : api.loginAccount(details);
    submit.then((profile) => {
      state.authUser = profile;
      state.authReady = true;
      state.authError = "";
      renderAuthNavigation();
      const nextRoute = safeBookingReturnRoute(parseRoute().next);
      location.hash = adminMode ? "#/admin" : nextRoute || "#/profile";
    }).catch((error) => {
      if (feedback) {
        const firstFieldError = Object.values(error.fieldErrors || {})[0];
        feedback.textContent = firstFieldError ? `${error.message}: ${firstFieldError}` : error.message;
        feedback.hidden = false;
      }
      button.disabled = false;
      button.innerHTML = `${signup ? "Create account" : adminMode ? "Admin log in" : "Log in"} ${icons.arrow}`;
    });
  });

  async function loadAuthState() {
    try {
      state.authUser = await api.getCurrentUser();
      state.authError = "";
    } catch (error) {
      state.authUser = null;
      state.authError = error.status === 401 ? "" : error.message;
    }
    state.authReady = true;
    renderAuthNavigation();
    const currentRoute = parseRoute();
    if (["profile", "bookings", "admin", "adminLogin", "ticket", "bookingStart", "seats", "checkout"].includes(currentRoute.name)) {
      routeTo(currentRoute);
    } else if (state.authUser && ["login", "signup"].includes(currentRoute.name) && safeBookingReturnRoute(currentRoute.next)) {
      location.hash = safeBookingReturnRoute(currentRoute.next);
    }
  }

  window.addEventListener("hashchange", () => routeTo(parseRoute()));
  document.addEventListener("visibilitychange", () => {
    if (document.hidden) stopSpotlightTimer();
    else startSpotlightTimer();
  });
  applyTheme(state.theme);
  routeTo(parseRoute());
  loadCityOptions();
  loadAuthState();
})();
