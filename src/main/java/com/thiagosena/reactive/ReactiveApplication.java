package com.thiagosena.reactive;

import com.thiagosena.reactive.model.PubSubMessage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Sinks;

@SpringBootApplication
public class ReactiveApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReactiveApplication.class, args);
    }

    @Bean
    public Sinks.Many<PubSubMessage> sink() { // like a producer
        return Sinks.many().multicast().onBackpressureBuffer(1000);
    }

}
