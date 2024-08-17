package com.example.mybooking.controller;

import com.example.mybooking.model.Language;
import com.example.mybooking.service.LanguageService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Контроллер для обработки запросов, связанных с языками.
 */
@Controller
public class LanguageController {

    @Autowired
    private LanguageService languageService;

    /*
     Отображение формы выбора языка.

     @param model объект Model для передачи данных в шаблон
     @return имя шаблона
     */
    @GetMapping("/languages")
    public String showLanguageForm(Model model) {
        List<Language> languages = languageService.getAllLanguages();
        model.addAttribute("languages", languages);
        return "language";
    }

    /*
     Обработка выбора языка и сохранение его в сессии.

     @param languageCode код выбранного языка
     @param session объект HttpSession для работы с сессией
     @return перенаправление на главную страницу
     */
    @PostMapping("/setLanguage")
    public String setLanguage(@RequestParam("language") String languageCode, HttpSession session) {
        session.setAttribute("language", languageCode);
        return "redirect:/";
    }
}