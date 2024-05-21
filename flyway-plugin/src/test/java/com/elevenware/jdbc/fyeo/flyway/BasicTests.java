package com.elevenware.jdbc.fyeo.flyway;

import com.elevenware.jdbc.fyeo.InMemorySecretStore;
import com.elevenware.jdbc.fyeo.JdbcFyeoDriver;
import com.elevenware.jdbc.fyeo.JdbcFyeoException;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class BasicTests {

    @TempDir
    static Path dataDir;

    static String jdbcUri;
    static String userName = "jamesbond";
    static String password = "moneypenny";
    static String userNameKey = "user.key";
    static String passwordKey = "password.key";

    @Test
    void flywayMigrateWorks() throws SQLException {

        DriverManager.registerDriver(new JdbcFyeoDriver());

        String localJdbcUri = String.format("jdbc:secret:h2:file:%s/test", dataDir.toAbsolutePath());
        InMemorySecretStore store = InMemorySecretStore.getInstance();
        store.store(userNameKey, userName);
        store.store(passwordKey, password);

        Flyway flyway = Flyway.configure()
                .dataSource(localJdbcUri, userNameKey, passwordKey).load();
        flyway.migrate();
    }

    @Test
    void flywayMigrateFailsWithPlainCreds() throws SQLException {

        DriverManager.registerDriver(new JdbcFyeoDriver());

        String localJdbcUri = String.format("jdbc:secret:h2:file:%s/test", dataDir.toAbsolutePath());
        InMemorySecretStore store = InMemorySecretStore.getInstance();
        store.store(userNameKey, userName);
        store.store(passwordKey, password);

        Flyway flyway = Flyway.configure()
                .dataSource(localJdbcUri, userName, password).load();
        assertThrows(JdbcFyeoException.class, () -> flyway.migrate());
    }


    @BeforeAll
    static void setup() throws SQLException {

        jdbcUri = String.format("jdbc:h2:file:%s/test", dataDir.toAbsolutePath());
        System.out.println(jdbcUri);

        String jdbcURL = String.format("%s;INIT=RUNSCRIPT FROM 'classpath:seed.sql';", jdbcUri);

        Connection connection = DriverManager.getConnection(jdbcURL, userName, password);
        connection.close();

    }


}
