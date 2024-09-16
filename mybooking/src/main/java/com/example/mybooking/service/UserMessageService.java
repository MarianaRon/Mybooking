package com.example.mybooking.service;

import com.example.mybooking.model.UserMessage;
import com.example.mybooking.repository.IUserMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
//
//@Service
//public class UserMessageService {
//
//    @Autowired
//    private IUserMessageRepository userMessageRepository;
//
//    public List<UserMessage> getAllMessages() {
//        return userMessageRepository.findAll();
//    }
//
//    public UserMessage saveMessage(UserMessage message) {
//        return userMessageRepository.save(message);
//    }
//
//    public void deleteMessage(Long id) {
//        userMessageRepository.deleteById(id);
//    }
//}

@Service
public class UserMessageService {

    @Autowired
    private IUserMessageRepository userMessageRepository;

    public void saveMessage(UserMessage userMessage) {
        userMessageRepository.save(userMessage);
    }

    public List<UserMessage> getAllMessages() {
        return userMessageRepository.findAll();
    }
    

    // Додаємо метод для видалення повідомлення за ID
    public void deleteMessage(Long id) {
        userMessageRepository.deleteById(id);
    }
}
