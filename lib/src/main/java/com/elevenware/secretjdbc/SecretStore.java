package com.elevenware.secretjdbc;

public interface SecretStore {

    String resolve(String key);

}
