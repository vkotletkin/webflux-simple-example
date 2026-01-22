package com.example.webfluxtest.event;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@RequiredArgsConstructor
@Service
public class DatabasePollingService {

    private final DataRepository dataRepository;
    private final Sinks.Many<ServerSentEvent<NotificationDTO>> notificationSink = Sinks.many().multicast().onBackpressureBuffer(10);
    private final AtomicLong counter = new AtomicLong();


    @PostConstruct
    public void init() {
        dataRepository.findLastRecord()
                .doOnNext(entity -> {
                            counter.set(entity.getId());
                            log.info("Initialized with last record id: {}", entity.getId());
                        }
                )
                .onErrorResume(e -> {
                    log.warn("Could not fetch last record, using default", e);
                    return Mono.empty();
                })
                .then(Mono.defer(this::startPolling))
                .subscribe();
    }

    private Mono<Void> startPolling() {

        Flux.interval(Duration.ofSeconds(30))
                .concatMap(tick -> pollDatabase())
                .subscribe();

        return Mono.empty();
    }

    @Transactional(readOnly = true)
    public Mono<Void> pollDatabase() {
        return dataRepository.existsByIdGreaterThan(counter.get())
                .flatMap(exists -> {
                    if (exists) {
                        // Если данные есть, тянем самую свежую запись, чтобы узнать её ID
                        return dataRepository.findLastRecord()
                                .doOnNext(latestEntity -> {
                                    // 1. Обновляем счетчик новым значением
                                    counter.set(latestEntity.getId());

                                    // 2. Отправляем уведомление
                                    String message = "New Data available. Latest ID: " + latestEntity.getId();
                                    NotificationDTO payload = new NotificationDTO(message);

                                    ServerSentEvent<NotificationDTO> sseEvent = ServerSentEvent.<NotificationDTO>builder()
                                            .event("data-update")
                                            .id(latestEntity.getId().toString())
                                            .data(payload)
                                            .build();

                                    notificationSink.tryEmitNext(sseEvent);

                                    log.info("Counter updated to {} and notification sent", latestEntity.getId());
                                });
                    }
                    log.info("Data is empty. Last ID: {}", counter.get());
                    return Mono.empty(); // Если новых данных нет, ничего не делаем
                })
                .then();
    }

    public Flux<ServerSentEvent<NotificationDTO>> getDataStream(String clientAddress) {
        return notificationSink.asFlux()
                .doOnSubscribe(s -> log.info("New SSE subscriber: {}", clientAddress))
                .doFinally(s -> log.info("SSE subscription ended: {}", s));
    }
}
