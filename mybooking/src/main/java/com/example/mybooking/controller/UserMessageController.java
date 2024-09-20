package com.example.mybooking.controller;

import com.example.mybooking.model.UserMessage;
import com.example.mybooking.service.UserMessageService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
@Controller
@RequestMapping("/users")
public class UserMessageController {

    @Autowired
    private UserMessageService userMessageService;

    @PostMapping("/sendUserMessage")
    public String sendMessage(
            @RequestParam String email,
            @RequestParam String topic,
            @RequestParam String message,
            Model model) {

        // Перевірка, чи всі поля заповнені
        if (email.isEmpty() || topic.isEmpty() || message.isEmpty()) {
            model.addAttribute("errorMessage", "Заповніть усі поля!");
            return "supports"; // Повертаємося на сторінку підтримки з повідомленням про помилку
        }

        // Створюємо та зберігаємо повідомлення
        UserMessage userMessage = new UserMessage(topic, message, email);
        userMessageService.saveMessage(userMessage);

        // Повідомлення про успішну відправку
        model.addAttribute("successMessage", "Лист успішно відправлений!");

        return "supports"; // Повертаємося на сторінку підтримки
    }
    // Метод для виведення всіх повідомлень
//    @GetMapping("/admin_message_list")
//    public String viewMessages(Model model) {
//        model.addAttribute("messages", userMessageService.getAllMessages());
//        return "admin_message_list"; // Сторінка з повідомленнями
//    }

    // Метод для видалення повідомлення за ID
    @PostMapping("/admin/messages/delete/{id}")
    public String deleteMessage(@PathVariable Long id) {
        userMessageService.deleteMessage(id);
        return "redirect:/admin_message_list"; // Повертаємося на сторінку з повідомленнями
    }
}
