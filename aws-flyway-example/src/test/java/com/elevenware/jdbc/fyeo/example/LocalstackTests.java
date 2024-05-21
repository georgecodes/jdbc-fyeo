package com.elevenware.jdbc.fyeo.example;

import com.elevenware.jdbc.fyeo.aws.ClientHolder;
import com.elevenware.jdbc.fyeo.JdbcFyeoDriver;
import org.flywaydb.core.Flyway;
import org.junit.AfterClass;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.containers.output.ToStringConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.CreateSecretRequest;

import java.nio.file.Path;
import java.sql.DriverManager;

@Testcontainers
public class LocalstackTests {

    static ToStringConsumer toStringConsumer = new ToStringConsumer();
    static String userName = "jamesbond";
    static String password = "moneypenny";
    @Container
    public static LocalStackContainer localstack = new LocalStackContainer(DockerImageName.parse("localstack/localstack"))
            .withServices(
                    LocalStackContainer.Service.SECRETSMANAGER
            )
        .withLogConsumer(toStringConsumer);

    @Container
    public static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer("postgres:11.1")
            .withDatabaseName("testdb")
            .withUsername(userName)
            .withPassword(password);

    @TempDir
    static Path dataDir;


    static String userNameKey = "user.key";
    static String passwordKey = "password.key";

    @Test
    void canPerformMigrationWithCredentialsInSecretsManager() {

        String jdbcUrl = postgreSQLContainer.getJdbcUrl();
        jdbcUrl = jdbcUrl.replace("jdbc:", "jdbc:secret.aws:");

        Flyway flyway = Flyway.configure()
                .dataSource(jdbcUrl, userNameKey, passwordKey)
                .load();
        flyway.migrate();
    }

    @BeforeAll
    static void setup() throws Exception {

        Region region = Region.of(localstack.getRegion());

        SecretsManagerClient secretsClient = SecretsManagerClient.builder()
                .region(region)
                .endpointOverride(localstack.getEndpoint())
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(localstack.getAccessKey(), localstack.getSecretKey())))
                .build();

        ClientHolder.setSecretsManagerClient(secretsClient);

        DriverManager.registerDriver(new JdbcFyeoDriver());

       secretsClient.createSecret(CreateSecretRequest.builder()
                .name(userNameKey)
                .secretString(userName)
                .build());

        secretsClient.createSecret(CreateSecretRequest.builder()
                .name(passwordKey)
                .secretString(password)
                .build());

    }

    @AfterClass
    public static void after(){
        System.out.println(toStringConsumer.toUtf8String());
    }

}
