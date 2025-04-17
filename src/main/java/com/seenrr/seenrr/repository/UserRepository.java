package com.seenrr.seenrr.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.seenrr.seenrr.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

}