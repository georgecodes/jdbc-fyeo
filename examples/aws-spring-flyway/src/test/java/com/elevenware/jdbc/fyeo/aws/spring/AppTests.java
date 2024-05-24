package com.elevenware.jdbc.fyeo.aws.spring;

import com.elevenware.jdbc.fyeo.InMemorySecretStore;
import com.elevenware.jdbc.fyeo.JdbcFyeoDriver;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class AppTests {

    @LocalServerPort
    private int port;

    static String userName = "jamesbond";
    static String password = "moneypenny";
    static String userNameKey = "user.secret.key";
    static String passwordKey = "password.secret.key";

    @Container
    public static PostgreSQLContainer psql = new PostgreSQLContainer("postgres:11.1")
            .withDatabaseName("testdb")
            .withUsername(userName)
            .withPassword(password);


    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void canFetchUsers() throws JsonProcessingException {

        ResponseEntity<String> response = restTemplate.exchange(absolutePath("/api/v1/users"), HttpMethod.GET, HttpEntity.EMPTY, String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        List<User> users = objectMapper.readValue(response.getBody(), List.class);

        assertEquals(2, users.size());

    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        final String jdbcUrl = psql.getJdbcUrl().replace("jdbc:", "jdbc:secret:");
        registry.add("spring.datasource.url", () -> jdbcUrl);
        registry.add("spring.datasource.username", () -> userNameKey);
        registry.add("spring.datasource.password", () -> passwordKey);
        registry.add("spring.datasource.driverClassName", () -> JdbcFyeoDriver.class.getCanonicalName());
        registry.add("spring.flyway.url", () -> jdbcUrl);
        registry.add("spring.flyway.user", () -> userNameKey);
        registry.add("spring.flyway.password", () -> passwordKey);
        registry.add("spring.flyway.driver-class-name", () -> JdbcFyeoDriver.class.getCanonicalName());
    }

    private String absolutePath(String relativePath) {
        return String.format("http://localhost:%d/%s", port, relativePath);
    }

    @BeforeAll
    static void beforeAll() {
        InMemorySecretStore.getInstance().store(userNameKey, userName);
        InMemorySecretStore.getInstance().store(passwordKey, password);
    }

}
