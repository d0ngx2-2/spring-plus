package org.example.expert.domain.todo.searchRepository;

import org.example.expert.domain.todo.dto.response.TodoSearchResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface SearchRepository {

    Page<TodoSearchResponse> search (
            String keyword,
            String managerNickname,
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    );
}
