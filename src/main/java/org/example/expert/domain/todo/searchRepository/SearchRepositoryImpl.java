package org.example.expert.domain.todo.searchRepository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.todo.dto.response.TodoSearchResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static org.example.expert.domain.comment.entity.QComment.comment;
import static org.example.expert.domain.manager.entity.QManager.manager;
import static org.example.expert.domain.todo.entity.QTodo.todo;
import static org.example.expert.domain.user.entity.QUser.user;

@RequiredArgsConstructor
public class SearchRepositoryImpl implements SearchRepository {

    private final JPAQueryFactory queryFactory;


    @Override
    public Page<TodoSearchResponse> search(
            String keyword,
            String managerNickname,
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    ) {
        List<TodoSearchResponse> content = queryFactory
                .select(Projections.constructor(
                        TodoSearchResponse.class,
                        todo.title,
                        manager.id.countDistinct(),
                        comment.id.countDistinct()
                ))
                .from(todo)
                .leftJoin(todo.managers, manager)
                .leftJoin(manager.user, user)
                .leftJoin(todo.comments, comment)
                .where(
                        titleContains(keyword),
                        manageNicknameContains(managerNickname),
                        createdAtBetween(start, end)
                )
                .groupBy(todo.id)
                .orderBy(todo.createdAt.desc())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(todo.id.count())
                .from(todo)
                .where(
                        titleContains(keyword),
                        createdAtBetween(start, end)
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    private BooleanExpression titleContains(String keyword) {
        return keyword == null ? null : todo.title.contains(keyword);
    }

    private BooleanExpression manageNicknameContains(String nickname) {
        return nickname == null ? null : user.nickName.contains(nickname);
    }

    private BooleanExpression createdAtBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null && end == null) {
            return null;
        }
        if (start != null && end != null) {
            return todo.createdAt.between(start, end);
        }
        if (start != null) {
            return todo.createdAt.goe(start);
        }
        return todo.createdAt.loe(end);
    }
}
