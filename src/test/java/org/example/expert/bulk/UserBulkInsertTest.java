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
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@MockBean(JwtUtil.class)
public class UserBulkInsertTest {

    @Autowired
    private DataSource dataSource;

    @Test
    @DisplayName("유저 500만 건 Bulk Insert")
    void bulkInsertUsers() throws Exception {

        String sql = """
            INSERT INTO users (nick_name, email, password, user_role, created_at, modified_at)
            VALUES (?, ?, ?, ?, now(), now())
        """;

        try (
                Connection con = dataSource.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {
            con.setAutoCommit(false);

            int batchSize = 1_000;
            int totalCount = 5_000_000;

            long start = System.currentTimeMillis();

            for (int i = 1; i <= totalCount; i++) {
                String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 12);

                ps.setString(1, "nick_" + uuid);
                ps.setString(2, "user" + uuid + "@test.com");
                ps.setString(3, "password");
                ps.setString(4, "USER");

                ps.addBatch();

                if (i % batchSize == 0) {
                    ps.executeBatch();
                    con.commit();
                    ps.clearBatch();
                }
            }

            ps.executeBatch();
            con.commit();

            long end = System.currentTimeMillis();
            System.out.println("총 소요 시간(ms) = " + (end - start));
        }
    }
}
