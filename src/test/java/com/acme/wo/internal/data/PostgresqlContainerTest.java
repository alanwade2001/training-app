package com.acme.wo.internal.data;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class PostgresqlContainerTest extends PostgresqlIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void applicationStartsAgainstPostgres() {

        Integer result =
                jdbcTemplate.queryForObject(
                        "select 1",
                        Integer.class
                );

        assertEquals(1, result);
    }
}
