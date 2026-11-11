package com.cineverse.service;

import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.cineverse.dto.DiscoveryMovieDto;
import com.cineverse.dto.DiscoveryMoviesPageDto;
import com.cineverse.dto.DiscoveryStatusDto;
import com.cineverse.entity.Movie;
import com.cineverse.exception.DiscoveryUnavailableException;
import com.cineverse.exception.InvalidRequestException;
import com.cineverse.repository.MovieRepository;

@Service
public class TmdbDiscoveryService {

    private static final String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500";
    private static final Map<Integer, String> GENRES = Map.ofEntries(
            Map.entry(12, "Adventure"),
            Map.entry(14, "Fantasy"),
            Map.entry(16, "Animation"),
            Map.entry(18, "Drama"),
            Map.entry(27, "Horror"),
            Map.entry(28, "Action"),
            Map.entry(35, "Comedy"),
            Map.entry(36, "History"),
            Map.entry(37, "Western"),
            Map.entry(53, "Thriller"),
            Map.entry(80, "Crime"),
            Map.entry(99, "Documentary"),
            Map.entry(878, "Science Fiction"),
            Map.entry(9648, "Mystery"),
            Map.entry(10402, "Music"),
            Map.entry(10749, "Romance"),
            Map.entry(10751, "Family"),
            Map.entry(10752, "War"),
            Map.entry(10770, "TV Movie"));

    private final RestClient restClient;
    private final MovieRepository movieRepository;
    private final String apiKey;

    public TmdbDiscoveryService(
            RestClient.Builder restClientBuilder,
            MovieRepository movieRepository,
            @Value("${tmdb.api-key:}") String apiKey,
            @Value("${tmdb.base-url:https://api.themoviedb.org/3}") String baseUrl) {
        var requestFactory = new org.springframework.http.client.JdkClientHttpRequestFactory(createHttpClient());
        requestFactory.setReadTimeout(Duration.ofSeconds(10));
        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
        this.movieRepository = movieRepository;
        this.apiKey = apiKey == null ? "" : apiKey.trim();
    }

    public DiscoveryStatusDto getStatus() {
        if (apiKey.isEmpty()) {
            return new DiscoveryStatusDto(
                    false,
                    "TMDB_API_KEY is not configured. Set it in the environment and restart to enable live movie discovery.",
                    "India");
        }
        return new DiscoveryStatusDto(
                true,
                "A TMDB API key is configured. Live movies are available; showtimes remain limited to local cinema listings.",
                "India");
    }

    public DiscoveryMoviesPageDto getMovies(String category, int page, String query) {
        requireApiKey();

        String normalizedQuery = query == null ? "" : query.trim();
        CategoryRequest categoryRequest = normalizedQuery.isEmpty()
                ? categoryRequest(category)
                : new CategoryRequest("/search/movie", Map.of("query", normalizedQuery, "region", "IN"));
        JsonNode response = request(categoryRequest, page);
        if (response == null) {
            throw upstreamFailure("TMDB returned an empty movie collection");
        }
        JsonNode results = response.path("results");
        if (!results.isArray()) {
            throw upstreamFailure("TMDB returned an invalid movie collection");
        }

        List<Movie> localMovies = movieRepository.findAllByOrderByTitleAsc();
        List<DiscoveryMovieDto> movies = new ArrayList<>();
        for (JsonNode result : results) {
            if (result.hasNonNull("id") && result.hasNonNull("title")) {
                movies.add(toDto(result, localMovies));
            }
        }

        return new DiscoveryMoviesPageDto(
                List.copyOf(movies),
                response.path("page").asInt(page),
                Math.max(1, Math.min(500, response.path("total_pages").asInt(1))));
    }

    public DiscoveryMovieDto getMovie(long id) {
        requireApiKey();
        JsonNode response = request("/movie/" + id, Map.of("language", "en-US"));
        if (response == null || !response.hasNonNull("id") || !response.hasNonNull("title")) {
            throw upstreamFailure("TMDB returned invalid movie details");
        }
        return toDto(response, movieRepository.findAllByOrderByTitleAsc());
    }

    private JsonNode request(CategoryRequest categoryRequest, int page) {
        Map<String, String> parameters = new LinkedHashMap<>(categoryRequest.parameters());
        parameters.put("page", String.valueOf(page));
        parameters.putIfAbsent("language", "en-US");
        return request(categoryRequest.path(), parameters);
    }

    private JsonNode request(String path, Map<String, String> parameters) {
        try {
            return restClient.get()
                    .uri(uriBuilder -> {
                        var builder = uriBuilder.path(path).queryParam("api_key", apiKey);
                        parameters.forEach(builder::queryParam);
                        return builder.build();
                    })
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientResponseException exception) {
            throw new DiscoveryUnavailableException(
                    "TMDB returned HTTP " + exception.getStatusCode().value() + ". Check the API key and try again.",
                    HttpStatus.BAD_GATEWAY,
                    exception);
        } catch (ResourceAccessException exception) {
            throw new DiscoveryUnavailableException(
                    "TMDB could not be reached. Check the network connection and try again.",
                    HttpStatus.BAD_GATEWAY,
                    exception);
        }
    }

    private static CategoryRequest categoryRequest(String category) {
        Map<String, String> parameters = new LinkedHashMap<>();
        parameters.put("region", "IN");
        parameters.put("include_adult", "false");

        return switch (category.toLowerCase(Locale.ROOT)) {
            case "all", "popular" -> new CategoryRequest("/movie/popular", parameters);
            case "trending" -> new CategoryRequest("/trending/movie/week", Map.of("language", "en-US"));
            case "upcoming" -> new CategoryRequest("/movie/upcoming", parameters);
            case "top-rated" -> new CategoryRequest("/movie/top_rated", parameters);
            case "bollywood" -> discover(parameters, "with_original_language", "hi");
            case "hollywood" -> discover(parameters, "with_original_language", "en");
            case "south-indian" -> discover(parameters, "with_original_language", "te|ta|ml|kn");
            case "korean" -> discover(parameters, "with_original_language", "ko");
            case "japanese" -> discover(parameters, "with_original_language", "ja");
            case "family-animation" -> discover(parameters, "with_genres", "10751,16");
            case "action-adventure" -> discover(parameters, "with_genres", "28,12");
            case "horror-thriller" -> discover(parameters, "with_genres", "27,53");
            case "romance-drama" -> discover(parameters, "with_genres", "10749,18");
            case "sci-fi-fantasy" -> discover(parameters, "with_genres", "878,14");
            default -> throw new InvalidRequestException(
                    "Unknown discovery category",
                    Map.of("category", "Choose a supported movie category"));
        };
    }

    private static CategoryRequest discover(
            Map<String, String> baseParameters,
            String parameter,
            String value) {
        Map<String, String> parameters = new LinkedHashMap<>(baseParameters);
        parameters.remove("region");
        parameters.put("sort_by", "popularity.desc");
        parameters.put(parameter, value);
        return new CategoryRequest("/discover/movie", parameters);
    }

    private DiscoveryMovieDto toDto(JsonNode movie, List<Movie> localMovies) {
        String title = movie.path("title").asText("Untitled film");
        Optional<Movie> localMatch = localMovies.stream()
                .filter(localMovie -> localMovie.getTitle().equalsIgnoreCase(title))
                .findFirst();

        List<String> genres = new ArrayList<>();
        JsonNode genreList = movie.has("genres") ? movie.path("genres") : movie.path("genre_ids");
        if (genreList.isArray()) {
            for (JsonNode genre : genreList) {
                String name = genre.hasNonNull("name")
                        ? genre.path("name").asText()
                        : GENRES.get(genre.asInt());
                if (name != null && !name.isBlank()) {
                    genres.add(name);
                }
            }
        }

        String posterPath = movie.path("poster_path").asText("").trim();
        String posterUrl = posterPath.isBlank()
                ? ""
                : posterPath.startsWith("https://") || posterPath.startsWith("http://")
                        ? posterPath
                        : IMAGE_BASE_URL + "/" + posterPath.replaceFirst("^/+", "");
        Movie linkedMovie = localMatch.orElse(null);

        return new DiscoveryMovieDto(
                movie.path("id").asText(),
                linkedMovie == null ? "TMDB" : "LOCAL",
                linkedMovie == null ? null : linkedMovie.getId(),
                title,
                movie.path("overview").asText(""),
                List.copyOf(genres),
                movie.path("original_language").asText(""),
                movie.hasNonNull("vote_average")
                        ? java.math.BigDecimal.valueOf(movie.path("vote_average").asDouble())
                        : null,
                posterUrl,
                movie.path("release_date").asText(""),
                linkedMovie != null);
    }

    private void requireApiKey() {
        if (apiKey.isEmpty()) {
            throw new DiscoveryUnavailableException(
                    "Live movie discovery is disabled. Set TMDB_API_KEY in the environment and restart CineVerse.",
                    HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    private static DiscoveryUnavailableException upstreamFailure(String message) {
        return new DiscoveryUnavailableException(message, HttpStatus.BAD_GATEWAY);
    }

    private static HttpClient createHttpClient() {
        HttpClient.Builder builder = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5));
        String proxyValue = Optional.ofNullable(System.getenv("HTTPS_PROXY"))
                .filter(value -> !value.isBlank())
                .orElse(System.getenv("HTTP_PROXY"));
        if (proxyValue != null && !proxyValue.isBlank()) {
            URI proxyUri = URI.create(proxyValue);
            if (proxyUri.getHost() == null || proxyUri.getPort() < 1) {
                throw new IllegalStateException("HTTPS_PROXY or HTTP_PROXY must include a proxy host and port");
            }
            builder.proxy(ProxySelector.of(new InetSocketAddress(proxyUri.getHost(), proxyUri.getPort())));
        }
        return builder.build();
    }

    private record CategoryRequest(String path, Map<String, String> parameters) {
    }
}
