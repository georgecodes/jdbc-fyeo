package com.elevenware.jdbc.fyeo;

public interface SecretStore {

    String resolve(String key);

}
