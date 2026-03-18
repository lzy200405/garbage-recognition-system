package edu.hubu.grs;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
@SpringBootTest
 public class DatabaseConnectionTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void testConnection() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            System.out.println("数据库连接成功！");
            System.out.println("数据库URL: " + conn.getMetaData().getURL());
            System.out.println("数据库用户名: " + conn.getMetaData().getUserName());

            // 测试简单查询
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            System.out.println("测试查询结果: " + result);
        }
    }
}