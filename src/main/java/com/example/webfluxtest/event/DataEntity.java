package com.example.webfluxtest.event;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Table("events")
public class DataEntity {

    @Id
    private Long id;

    private String data;
    private LocalDateTime createdAt;
}
