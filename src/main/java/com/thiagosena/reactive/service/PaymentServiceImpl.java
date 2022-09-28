package com.thiagosena.reactive.service;

import com.thiagosena.reactive.model.Payment;
import com.thiagosena.reactive.model.PaymentStatus;
import com.thiagosena.reactive.repository.GenericRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Optional;
import java.util.UUID;

@Component
public class PaymentServiceImpl implements PaymentService {
    private final GenericRepository database;

    private final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class.getName());

    public PaymentServiceImpl(GenericRepository database) {
        this.database = database;
    }

    @Override
    public Mono<Payment> createPayment(final String userId) {
        final Payment payment = new Payment(UUID.randomUUID().toString(), userId, PaymentStatus.PENDING);

        return Mono.fromCallable(() -> {
                    log.info("Saving payment transaction for user {}", userId);
                    return this.database.save(userId, payment);
                })
                .subscribeOn(Schedulers.boundedElastic())
                .doOnNext(received -> log.info("Payment received {}", received.userId()));
    }

    @Override
    public Mono<Payment> getPayment(final String userId) {
        return Mono
                .defer(() -> {
                    log.info("Getting payment from database - {}", userId);
                    final Optional<Payment> payment = this.database.get(userId, Payment.class);
                    return Mono.justOrEmpty(payment);
                })
                .subscribeOn(Schedulers.boundedElastic())
                .doOnNext(it -> log.info("Payment received - {}", userId));
    }

    @Override
    public Mono<Payment> processPayment(final String key, final PaymentStatus status) {
        log.info("On payment {} received to status {}", key, status);
        return getPayment(key)
                .flatMap(payment -> Mono.fromCallable(() -> {
                                    log.info("Processing payment {} to status {}", key, status);
                                    return this.database.save(key, new Payment(payment.id(), payment.userId(), status));
                                })
                                .subscribeOn(Schedulers.boundedElastic())
                );
    }
}
