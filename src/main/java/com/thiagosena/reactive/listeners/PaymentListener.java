package com.thiagosena.reactive.listeners;

import com.thiagosena.reactive.model.PaymentStatus;
import com.thiagosena.reactive.model.PubSubMessage;
import com.thiagosena.reactive.service.PaymentServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Sinks;

@Component
public class PaymentListener implements InitializingBean {
    private final Logger log = LoggerFactory.getLogger(PaymentListener.class.getName());

    private final Sinks.Many<PubSubMessage> sink;
    private final PaymentServiceImpl repository;

    public PaymentListener(Sinks.Many<PubSubMessage> sink, PaymentServiceImpl repository) {
        this.sink = sink;
        this.repository = repository;
    }

    @Override
    public void afterPropertiesSet() {
        this.sink.asFlux()
                .subscribe(
                        next -> {
                            log.info("On next message - {}", next.key());
                            this.repository.processPayment(next.key(), PaymentStatus.APPROVED)
                                    .doOnNext(it -> log.info("Payment processed on listener"))
                                    .subscribe();
                        },
                        error -> log.error("On pub-sub listener observe error", error),
                        () -> log.info("On pub-sub listener complete")
                );
    }
}
