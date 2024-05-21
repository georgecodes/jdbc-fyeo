package com.elevenware.jdbcsecret.aws;

import com.elevenware.secretjdbc.SecretStore;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

public class AwsSecretStore implements SecretStore {

    private SecretsManagerClient secretsClient;

    public AwsSecretStore() {
        secretsClient = ClientHolder.getSecretsManagerClient();
        if(secretsClient != null) {
            return;
        }
        secretsClient = SecretsManagerClient
                .builder()
                .build();
    }

    @Override
    public String resolve(String key) {
        GetSecretValueResponse secretValue = secretsClient.getSecretValue(GetSecretValueRequest.builder()
                .secretId(key)
                .build());
        return secretValue.secretString();
    }
}
