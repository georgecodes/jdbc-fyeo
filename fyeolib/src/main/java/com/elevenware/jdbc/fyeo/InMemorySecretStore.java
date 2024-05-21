package com.elevenware.jdbc.fyeo;

import java.util.HashMap;
import java.util.Map;

public class InMemorySecretStore implements SecretStore {

    private static final InMemorySecretStore INSTANCE = new InMemorySecretStore();

    private InMemorySecretStore() {}

    private Map<String, String> store = new HashMap<>();

    public static InMemorySecretStore getInstance() {
        return INSTANCE;
    }

    public void store(String key, String val) {
        store.put(key, val);
    }

    @Override
    public String resolve(String key) {
        if(!store.containsKey(key)) {
            throw new JdbcFyeoException(String.format("Missing value %s", key));
        }
        return store.get(key);
    }
}
