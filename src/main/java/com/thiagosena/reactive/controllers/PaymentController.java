package com.thiagosena.reactive.controllers;

import com.thiagosena.reactive.controllers.payloads.NewPaymentInput;
import com.thiagosena.reactive.model.Payment;
import com.thiagosena.reactive.publishers.PaymentPublisher;
import com.thiagosena.reactive.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("payments")
public class PaymentController {
    private final Logger log = LoggerFactory.getLogger(PaymentController.class.getName());

    private final PaymentService service;
    private final PaymentPublisher publisher;

    public PaymentController(PaymentService service, PaymentPublisher publisher) {
        this.service = service;
        this.publisher = publisher;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Payment> createPayment(@RequestBody final NewPaymentInput input) {
        final String userId = input.userId();
        log.info("Payment to be processed {}", userId);
        return this.service.createPayment(userId)
                .flatMap(this.publisher::onPaymentCreate)
//                .flatMap(payment ->
//                        Flux.interval(Duration.ofSeconds(1))
//                                .doOnNext(it -> log.info("Next tick - {}", it))
//                                .flatMap(tick -> this.service.getPayment(userId))
//                                .filter(it -> PaymentStatus.APPROVED == it.status())
//                                .next()
//                )
                .doOnNext(p -> log.info("Payment processed {}", userId))
                .timeout(Duration.ofSeconds(5))
                .retryWhen(
                        Retry.backoff(2, Duration.ofSeconds(1))
                                .doAfterRetry(signal -> log.info("Execution failed... retrying... {}", signal.totalRetries()))
                );
    }

    @GetMapping(value = "users")
    public Flux<Payment> findAllById(@RequestParam String ids) {
        final List<String> _ids = Arrays.asList(ids.split(","));
        log.info("Collecting {} payments", _ids.size());
        return Flux.fromIterable(_ids)
                .flatMap(this.service::getPayment, 2000);
    }

}
