package com.example.mybooking.repository;

import com.example.mybooking.model.Room;
import com.example.mybooking.model.UserMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IUserMessageRepository extends JpaRepository<UserMessage, Long> {
}