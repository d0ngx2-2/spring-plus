package org.example.expert.domain.log.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Log {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String action;

    private Long userId;

    private Long todoId;

    private LocalDateTime createdAt;

    public Log(String action, Long userId, Long todoId) {
        this.action = action;
        this.userId = userId;
        this.todoId = todoId;
        this.createdAt = LocalDateTime.now();
    }
}
