package com.example.mybooking.controller;

import com.example.mybooking.model.Partner;
import com.example.mybooking.service.PartnerService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/partners")
public class PartnerController {

    @Autowired
    private PartnerService partnerService;

    // Получение списка всех партнеров
    @GetMapping
    public List<Partner> getAllPartners() {
        return partnerService.getAllPartners();
    }

    // Получение партнера по ID
    @GetMapping("/{id}")
    public Optional<Partner> getPartnerById(@PathVariable Long id) {
        return partnerService.getPartnerById(id);
    }

    // Отображение формы для создания нового аккаунта партнера
    @GetMapping("/new")
    public String showPartnerForm() {
        return "partner_Account";
    }

    // Отображение формы для входа партнера
    @GetMapping("/login")
    public String showLoginForm() {
        return "exit_Account";
    }

    // Обработка входа партнера
    @PostMapping("/login")
    public String loginPartner(@RequestParam String email, @RequestParam String password, HttpSession session, Model model) {
        Optional<Partner> partner = partnerService.findByEmail(email);

        // Проверка наличия партнера и соответствия пароля
        if (partner.isPresent() && partner.get().getPassword().equals(password)) {
            session.setAttribute("partnerEmail", partner.get().getEmail()); // Сохранение email партнера в сессии
            session.setAttribute("userName", partner.get().getFirstName()); // (Дополнительно) Сохранение имени партнера в сессии
            return "redirect:/home_partners"; // перенаправление на главную страницу после успешного входа
        } else {
            model.addAttribute("errorMessage", "Invalid email or password");
            return "partner_login"; // возвращает на форму с ошибкой
        }
    }

    // Обработка создания нового партнера
    @PostMapping
    public String createPartner(
            @ModelAttribute Partner partner,
            @RequestParam("confirmPassword") String confirmPassword,
            Model model) {

        // Проверка совпадения паролей
        if (!partner.getPassword().equals(confirmPassword)) {
            model.addAttribute("errorMessage", "Passwords do not match");
            return "partner_Account"; // возвращает на форму с ошибкой
        }

        // Проверка длины пароля
        if (partner.getPassword().length() < 6) {
            model.addAttribute("errorMessage", "Password must be at least 6 characters long");
            return "partner_Account"; // возвращает на форму с ошибкой
        }

        // Создание нового партнера
        partnerService.createPartner(partner);
        return "redirect:/home_partners"; // перенаправление после успешного создания
    }

    // Удаление партнера по ID
    @DeleteMapping("/{id}")
    public void deletePartner(@PathVariable Long id) {
        partnerService.deletePartner(id);
    }

    // Выход партнера
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // Инвалидация сессии
        return "redirect:/partners/login"; // перенаправление на страницу входа
    }
}