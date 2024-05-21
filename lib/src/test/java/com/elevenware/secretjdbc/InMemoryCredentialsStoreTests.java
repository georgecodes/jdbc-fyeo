package com.elevenware.secretjdbc;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InMemoryCredentialsStoreTests {

    @TempDir
    static Path dataDir;

    static String jdbcUri;
    static String userName = "george";
    static String password = "somepassword";
    static String userNameKey = "user.key";
    static String passwordKey = "password.key";

    @Test
    void failsIfPlainCredentialsUsed() throws SQLException {

        DriverManager.registerDriver(new SecretJdbcDriver());

        String localJdbcUri = String.format("jdbc:secret:h2:file:%s/test", dataDir.toAbsolutePath());
        InMemorySecretStore store = InMemorySecretStore.getInstance();
        store.store(userNameKey, userName);
        store.store(passwordKey, password);

        assertThrows(SecretJdbcConfigurationException.class, () -> DriverManager.getConnection(localJdbcUri, userName, password));

    }

    @Test
    void canPullCredentialsFromStore() throws SQLException {

        DriverManager.registerDriver(new SecretJdbcDriver());

        String localJdbcUri = String.format("jdbc:secret:h2:file:%s/test", dataDir.toAbsolutePath());
        InMemorySecretStore store = InMemorySecretStore.getInstance();
        store.store(userNameKey, userName);
        store.store(passwordKey, password);

        Connection connection = DriverManager.getConnection(localJdbcUri, userNameKey, passwordKey);
        DatabaseMetaData metaData = connection.getMetaData();
        Set<String> tables = new HashSet<>();
        try(ResultSet resultSet = metaData.getTables(null, null, null, new String[]{"TABLE"})){
            while(resultSet.next()) {
                String tableName = resultSet.getString("TABLE_NAME");
                tables.add(tableName);
            }
        }
        connection.close();
        assertTrue(tables.contains("APP_USERS"));

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
