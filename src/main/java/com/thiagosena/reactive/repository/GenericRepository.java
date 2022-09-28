package com.thiagosena.reactive.repository;

import java.util.Optional;

public interface GenericRepository {
    <T> T save(final String key, final T value);

    <T> Optional<T> get(final String key, final Class<T> clazz);
}
