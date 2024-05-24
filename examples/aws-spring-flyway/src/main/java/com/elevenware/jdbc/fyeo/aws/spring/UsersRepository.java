package com.elevenware.jdbc.fyeo.aws.spring;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UsersRepository extends JpaRepository<User, String> {
}
