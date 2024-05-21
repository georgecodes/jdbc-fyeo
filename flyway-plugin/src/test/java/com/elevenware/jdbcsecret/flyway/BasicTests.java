package com.elevenware.jdbcsecret.flyway;

import com.elevenware.secretjdbc.InMemorySecretStore;
import com.elevenware.secretjdbc.SecretJdbcDriver;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class BasicTests {

    @TempDir
    static Path dataDir;

    static String jdbcUri;
    static String userName = "george";
    static String password = "somepassword";
    static String userNameKey = "user.key";
    static String passwordKey = "password.key";


    @Test
    void it() throws SQLException {

        DriverManager.registerDriver(new SecretJdbcDriver());

        String localJdbcUri = String.format("jdbc:secret:h2:file:%s/test", dataDir.toAbsolutePath());
        InMemorySecretStore store = InMemorySecretStore.getInstance();
        store.store(userNameKey, userName);
        store.store(passwordKey, password);

        Flyway flyway = Flyway.configure()
                .dataSource(localJdbcUri, userNameKey, passwordKey).load();
        flyway.migrate();
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
