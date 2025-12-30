package org.example.expert.bulk;

import org.example.expert.config.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.UUID;

@SpringBootTest
// MockMvc 자동 설정 (보안 필터 비활성화)
@AutoConfigureMockMvc(addFilters = false)

// test 프로파일 사용 (application-test.yml)
@ActiveProfiles("test")

// JwtUtil 빈을 Mock 처리하여 실제 JWT 로직 실행 방지
@MockBean(JwtUtil.class)
public class UserBulkInsertTest {

    // Spring이 관리하는 DataSource 주입
    @Autowired
    private DataSource dataSource;

    @Test
    @DisplayName("유저 500만 건 Bulk Insert")
    void bulkInsertUsers() throws Exception {

        // 유저 데이터를 INSERT 하기 위한 SQL
        // created_at, modified_at 은 DB 현재 시간 사용
        String sql = """
            INSERT INTO users (nick_name, email, password, user_role, created_at, modified_at)
            VALUES (?, ?, ?, ?, now(), now())
        """;

        // Connection, PreparedStatement 자동 닫아주기
        try (
                // 커넥션 획득
                Connection con = dataSource.getConnection();

                // SQL을 미리 컴파일한 PreparedStatement 생성
                PreparedStatement ps = con.prepareStatement(sql)
        ) {
            // AutoCommit 비활성화 → Batch Insert 성능 향상을 위함!!!
            con.setAutoCommit(false);

            // 한 번에 실행할 batch 크기
            int batchSize = 1_000;

            // 총 삽입할 데이터 수 (500만 건)
            int totalCount = 5_000_000;

            // 시작 시간 기록 (성능 측정용)
            long start = System.currentTimeMillis();

            // 1부터 500만까지 반복
            for (int i = 1; i <= totalCount; i++) {

                // UUID 생성 후 "-" 제거, 길이 축약시키기
                // → nick/email 중복 방지
                String uuid = UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 12);

                // PreparedStatement 파라미터 바인딩
                ps.setString(1, "nick_" + uuid);               // 닉네임
                ps.setString(2, "user" + uuid + "@test.com"); // 이메일
                ps.setString(3, "password");                  // 비밀번호
                ps.setString(4, "USER");                      // 권한

                // Batch에 SQL 추가
                ps.addBatch();

                // batchSize(1000)마다 실행
                if (i % batchSize == 0) {
                    ps.executeBatch(); // 누적된 batch 실행
                    con.commit();      // 트랜잭션 커밋
                    ps.clearBatch();   // batch 초기화
                }
            }

            // 반복문 종료 후 남아있는 batch 실행
            ps.executeBatch();
            con.commit();

            // 종료 시간 기록
            long end = System.currentTimeMillis();

            // 총 소요 시간 출력
            System.out.println("총 소요 시간(ms) = " + (end - start));
        }
    }
}

/// 참고 문헌.. https://docs.spring.io/spring-framework/reference/data-access/jdbc/advanced.html [배치작업 부분]
/// 티스토리 https://blogshine.tistory.com/686?utm_source=chatgpt.com
