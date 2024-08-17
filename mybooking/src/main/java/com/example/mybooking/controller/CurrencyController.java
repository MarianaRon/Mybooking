package com.example.mybooking.controller;


import com.example.mybooking.model.Currency;
import com.example.mybooking.service.CurrencyService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class CurrencyController {

    @Autowired // Автоматическая настройка зависимости
    private CurrencyService currencyService;

    // Метод для отображения формы выбора валюты
    @GetMapping("/currencies")
    public String showCurrencyForm(Model model) {
        List<Currency> currencies = currencyService.getAllCurrencies(); // Получаем все доступные валюты
        model.addAttribute("currencies", currencies); // Добавляем список валют в модель
        return "/currency"; // Возвращаем имя шаблона
    }

    // Метод для обработки выбора валюты и сохранения в сессии
    @PostMapping("/setCurrency")
    public String setCurrency(@RequestParam("currency") String currencyCode, HttpSession session) {
        session.setAttribute("currency", currencyCode); // Сохраняем выбранную валюту в сессии
        return "redirect:/"; // Перенаправляем пользователя на главную страницу
    }

    // Метод для отображения главной страницы
//    @GetMapping("/")
//    public String home(HttpSession session, Model model) {
//        String currency = (String) session.getAttribute("currency"); // Получаем выбранную валюту из сессии
//        model.addAttribute("currency", currency); // Добавляем выбранную валюту в модель
//        return "home"; // Возвращаем имя шаблона
//    }
}