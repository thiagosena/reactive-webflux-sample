package com.thiagosena.reactive.publishers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thiagosena.reactive.model.Payment;
import com.thiagosena.reactive.model.PubSubMessage;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

@Component
public class PaymentPublisher {
    private final Sinks.Many<PubSubMessage> sink;

    private final ObjectMapper mapper;

    public PaymentPublisher(Sinks.Many<PubSubMessage> sink, ObjectMapper mapper) {
        this.sink = sink;
        this.mapper = mapper;
    }

    public Mono<Payment> onPaymentCreate(final Payment payment) {
        return Mono.fromCallable(() -> {
                    final String userId = payment.userId();
                    final String data = mapper.writeValueAsString(payment);
                    return new PubSubMessage(userId, data);
                })
                .subscribeOn(Schedulers.parallel())
                .doOnNext(this.sink::tryEmitNext)
                .thenReturn(payment);
    }

}
