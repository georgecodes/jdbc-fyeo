package com.elevenware.jdbc.fyeo.aws;

import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;

public class ClientHolder {

    private static SecretsManagerClient secretsManagerClient;

    public static void setSecretsManagerClient(SecretsManagerClient client) {
        secretsManagerClient = client;
    }

    public static SecretsManagerClient getSecretsManagerClient() {
        return secretsManagerClient;
    }
}
