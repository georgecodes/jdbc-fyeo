package com.elevenware.jdbc.fyeo.aws.spring;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

//@Service
public class InMemoryUserService implements UserService {
    @Override
    public List<User> allUsers() {
        return List.of(User.builder()
                        .id(UUID.randomUUID())
                        .displayName("Johnny Bananas")
                        .email("dr.johnny.bananas@hardsums.com")
                .build());
    }
}
