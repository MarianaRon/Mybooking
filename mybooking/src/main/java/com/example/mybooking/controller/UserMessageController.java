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
            @RequestParam String topic,
            @RequestParam String message,
            HttpSession session,
            Model model) {

        // Перевіряємо, чи користувач зареєстрований
        Object currentUser = session.getAttribute("currentUser");

        if (currentUser == null) {
            model.addAttribute("notRegistered", true);
            return "supports"; // Повертаємося на сторінку підтримки з повідомленням про помилку
        }

        // Отримуємо email користувача із сесії
        String email = (String) session.getAttribute("currentUserEmail");

        if (email == null || email.isEmpty()) {
            model.addAttribute("errorMessage", "Не вдалося отримати email. Будь ласка, увійдіть у систему.");
            return "supports"; // Повертаємося на сторінку підтримки з помилкою
        }

        // Створюємо та зберігаємо повідомлення
        UserMessage userMessage = new UserMessage(topic, message, email);
        userMessageService.saveMessage(userMessage);

        // Повідомлення про успішну відправку
        model.addAttribute("successMessage", "Лист успішно відправлений!");

        return "supports"; // Повертаємося на сторінку підтримки
    }


    // Адмінська частина для перегляду всіх повідомлень
    @GetMapping("/admin/messages")
    public String viewMessages(Model model) {
        model.addAttribute("messages", userMessageService.getAllMessages());
        return "admin_message_list"; // Сторінка адміна для відображення всіх повідомлень
    }

    // Видалення повідомлення адміном
    @PostMapping("/admin/messages/delete/{id}")
    public String deleteMessage(@PathVariable Long id) {
        userMessageService.deleteMessage(id);
        return "redirect:/users/admin/messages";
    }
}
