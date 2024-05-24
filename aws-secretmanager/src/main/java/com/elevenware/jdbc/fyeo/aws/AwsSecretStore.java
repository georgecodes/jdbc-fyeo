package com.elevenware.jdbc.fyeo.aws;

import com.elevenware.jdbc.fyeo.SecretStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

public class AwsSecretStore implements SecretStore {

    private static Logger LOG = LoggerFactory.getLogger(AwsSecretStore.class);

    private SecretsManagerClient secretsClient;

    public AwsSecretStore() {
        secretsClient = ClientHolder.getSecretsManagerClient();
        if(secretsClient != null) {
            LOG.info("Using client from holder {}", secretsClient);
            return;
        }
        LOG.info("Building client");
        secretsClient = SecretsManagerClient
                .builder()
                .region(Region.EU_WEST_2)
                .build();
        LOG.info("Client built {}", secretsClient);
    }

    @Override
    public String resolve(String key) {
        LOG.info("Request to resolve secret {}", key);
        try {
            GetSecretValueResponse secretValue = secretsClient.getSecretValue(GetSecretValueRequest.builder()
                    .secretId(key)
                    .build());
            return secretValue.secretString();
        } catch (Exception e) {
            LOG.error("no", e);
            throw new RuntimeException(e);
        }

    }
}
