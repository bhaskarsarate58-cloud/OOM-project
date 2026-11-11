package com.cineverse.dto;

public record DiscoveryStatusDto(
        boolean liveAvailable,
        String message,
        String region) {
}
