package com.vehicleinspection.dto;

public record LoginResponse(Long id, String name, String email, String role, String message) {
}
