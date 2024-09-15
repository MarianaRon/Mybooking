package com.example.mybooking.repository;

import com.example.mybooking.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IUserRepository extends JpaRepository<User, Long> {
    List<User> findBySubscribedToNewsletterTrue();

    User findByUsername(String username);

    User findByEmail(String email);

}