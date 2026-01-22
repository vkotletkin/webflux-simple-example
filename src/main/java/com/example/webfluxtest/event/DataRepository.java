package com.example.webfluxtest.event;

import org.springframework.data.domain.Limit;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public interface DataRepository extends ReactiveCrudRepository<DataEntity, Long> {
    Mono<Boolean> existsByIdGreaterThan(Long idIsGreaterThan);

    @Query("SELECT * FROM events ORDER BY id DESC LIMIT 1")
    Mono<DataEntity> findLastRecord();
}
