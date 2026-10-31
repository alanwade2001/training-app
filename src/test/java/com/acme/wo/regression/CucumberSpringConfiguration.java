package com.acme.wo.regression;

import com.acme.wo.internal.data.PostgresqlTestConfiguration;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;

@CucumberContextConfiguration
@SpringBootTest
@AutoConfigureMockMvc
@Import(PostgresqlTestConfiguration.class)
public class CucumberSpringConfiguration {
}