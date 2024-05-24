package com.elevenware.jdbc.fyeo.aws.spring;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JpaUserService implements UserService {

    private UsersRepository usersRepository;

    public JpaUserService(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    @Override
    public List<User> allUsers() {
        return usersRepository.findAll();
    }
}
