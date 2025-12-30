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

    /**
     * Todo 동적 검색 + 페이징 조회
     */
    @Override
    public Page<TodoSearchResponse> search(
            String keyword,                // Todo 제목 검색 키워드 (이거 왜 초록색이지..?)
            String managerNickname,        // 담당자 닉네임
            LocalDateTime start,           // 생성일 시작
            LocalDateTime end,             // 생성일 종료
            Pageable pageable              // 페이지 정보
    ) {

        // 실제 페이지에 담길 데이터 조회 쿼리
        List<TodoSearchResponse> content = queryFactory
                // DTO 생성자 방식으로 결과 매핑
                .select(Projections.constructor(
                        TodoSearchResponse.class,
                        todo.title,                       // Todo 제목
                        manager.id.countDistinct(),       // 담당자 수
                        comment.id.countDistinct()        // 댓글 수
                ))
                // Todo 기준 조회
                .from(todo)

                // Todo - Manager 다대다 관계 LEFT JOIN
                .leftJoin(todo.managers, manager)

                // Manager - User 연관관계 JOIN
                .leftJoin(manager.user, user)

                // Todo - Comment 연관관계 LEFT JOIN
                .leftJoin(todo.comments, comment)

                // 동적 조건절 (null 조건은 자동 무시됨)
                .where(
                        titleContains(keyword),
                        manageNicknameContains(managerNickname),
                        createdAtBetween(start, end)
                )

                // 집계 함수(countDistinct)를 사용하므로 groupBy 필수
                .groupBy(todo.id)
                // 최신 Todo - 우선정렬
                .orderBy(todo.createdAt.desc())
                // 페이지 크기만큼 조회
                .limit(pageable.getPageSize())
                .fetch();

        /**
         * 전체 데이터 개수 조회 쿼리
         * - Page 구현을 위해 필수
         * - JOIN / GROUP BY 제거하여 성능 최적화
         */
        Long total = queryFactory
                .select(todo.id.count())
                .from(todo)
                .where(
                        titleContains(keyword),
                        createdAtBetween(start, end)
                )
                .fetchOne();

        // Page 객체로 감싸서 반환
        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    /**
     * 제목 키워드 검색 조건
     * - keyword가 null이면 조건 제외
     */
    private BooleanExpression titleContains(String keyword) {
        return keyword == null ? null : todo.title.contains(keyword);
    }

    /**
     * 담당자 닉네임 검색 조건
     * - manager → user 조인 기준
     * - null이면 조건 제외
     */
    private BooleanExpression manageNicknameContains(String nickname) {
        return nickname == null ? null : user.nickName.contains(nickname);
    }

    /**
     * 생성일 범위 검색 조건
     * - start, end 둘 다 없으면 조건 제외
     * - 한쪽만 있으면 >= 또는 <= 조건 적용
     */
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
