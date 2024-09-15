package com.example.mybooking.service;

import com.example.mybooking.model.User;
import com.example.mybooking.repository.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private IUserRepository userRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public boolean checkPassword(User user, String oldPassword) {
        return user.getPassword().equals(oldPassword); // Перевірка старого паролю
    }

    public void changePassword(User user, String newPassword) {
        user.setPassword(newPassword);
        userRepository.save(user); // Збереження нового паролю
    }
    public void subscribeUser(User user) {
        user.setSubscribedToNewsletter(true);
        userRepository.save(user);
    }

    public List<String> getAllSubscribers() {
        return userRepository.findBySubscribedToNewsletterTrue()
                .stream()
                .map(User::getEmail)  // Витягуємо лише email користувачів
                .collect(Collectors.toList());
    }


}