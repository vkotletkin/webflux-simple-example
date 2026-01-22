package com.example.webfluxtest.event;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class EventController {

    private final DatabasePollingService databasePollingService;

    @GetMapping(value = "/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<NotificationDTO>> subscribeToUpdates(ServerHttpRequest request) {
        String clientAddress = request.getRemoteAddress() != null ? request.getRemoteAddress().getHostString() : "unknown";
        Flux<ServerSentEvent<NotificationDTO>> heartbeatFlux = Flux.interval(Duration.ofSeconds(15))
                .map(i -> ServerSentEvent.<NotificationDTO>builder()
                        .comment("heartbeat " + LocalDateTime.now()) // Комментарии игнорируются клиентом, но держат канал открытым
                        .build());
        Flux<ServerSentEvent<NotificationDTO>> events = databasePollingService.getDataStream(clientAddress);
        return Flux.merge(heartbeatFlux, events);
    }
}
