package com.thiagosena.reactive.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryRepository implements GenericRepository {

    private static final Map<String, String> DATABASE = new ConcurrentHashMap<>();

    private final ObjectMapper mapper;

    public InMemoryRepository(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public <T> T save(final String key, final T value) {
        try {
            final var data = this.mapper.writeValueAsString(value);
            DATABASE.put(key, data);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return value;
    }

    @Override
    public <T> Optional<T> get(final String key, final Class<T> clazz) {
        final String json = DATABASE.get(key);
        return Optional.ofNullable(json).map(data -> {
            try {
                return mapper.readValue(data, clazz);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
