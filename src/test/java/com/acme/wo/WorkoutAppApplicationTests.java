package com.acme.wo;

import com.acme.wo.internal.data.PostgresqlTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(PostgresqlTestConfiguration.class)
class WorkoutAppApplicationTests {

    @Test
    void contextLoads() {
    }

}
