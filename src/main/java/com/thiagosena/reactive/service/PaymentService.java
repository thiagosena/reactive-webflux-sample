package com.thiagosena.reactive.service;

import com.thiagosena.reactive.model.Payment;
import com.thiagosena.reactive.model.PaymentStatus;
import reactor.core.publisher.Mono;

public interface PaymentService {
    Mono<Payment> createPayment(String userId);

    Mono<Payment> getPayment(String userId);

    Mono<Payment> processPayment(String key, PaymentStatus status);
}
