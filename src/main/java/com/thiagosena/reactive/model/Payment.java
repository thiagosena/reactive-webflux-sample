package com.thiagosena.reactive.model;

public record Payment(String id, String userId, PaymentStatus status) {
}
