package com.elevenware.jdbc.fyeo;

public class EnvVarSecretStore implements SecretStore {


    @Override
    public String resolve(String key) {
        return System.getenv(key);
    }
}
