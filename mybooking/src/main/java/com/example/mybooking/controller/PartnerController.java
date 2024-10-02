package com.example.mybooking.controller;

import com.example.mybooking.model.Hotel;
import com.example.mybooking.model.Partner;
import com.example.mybooking.service.HotelService;
import com.example.mybooking.service.PartnerService;
import com.example.mybooking.service.UserService;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.List;

@Controller
@RequestMapping("/partners")
public class PartnerController {

    @Autowired
    private PartnerService partnerService;

    @Autowired
    private HotelService hotelService;

    @Autowired
    private UserService userService;

    // Получение списка всех партнеров
    @GetMapping("/partner_list")
    public String getAllPartners(Model model) {
        List<Partner> partners = partnerService.getAllPartners();
        model.addAttribute("partners", partners);
        return "partners/partner_list";
    }

    // Получение информации о партнере по ID
    @GetMapping("/{id}")
    public String getPartnerById(@PathVariable Long id, Model model) {
        Optional<Partner> partner = partnerService.getPartnerById(id);
        if (partner.isPresent()) {
            model.addAttribute("partner", partner.get());
            return "partners/partner_details";
        } else {
            return "redirect:/partners";
        }
    }

    // Отображение формы для создания нового партнера
    @GetMapping("/new")
    public String showPartnerForm(Model model) {
        model.addAttribute("partner", new Partner());
        return "partners/partner_form";
    }

    // Отображение формы входа
    @GetMapping("/login")
    public String showLoginForm() {
        return "partners/partner_login";
    }

    // Обработка логина партнера
    @PostMapping("/login")
    public String loginPartner(@RequestParam String email, @RequestParam String password, HttpSession session, Model model) {
        Optional<Partner> optionalPartner = partnerService.findByEmail(email);

        if (optionalPartner.isPresent()) {
            Partner partner = optionalPartner.get();
            if (partner.getPassword().equals(password)) {
                session.setAttribute("loggedInPartner", partner);
                session.setAttribute("userName", partner.getFirstName());
                return "redirect:/home_partners";
            } else {
                model.addAttribute("errorMessage", "Неправильный email или пароль");
                return "partner_Account";
            }
        } else {
            model.addAttribute("errorMessage", "Партнер с таким email не найден.");
            return "partner_Account";
        }
    }

    // Создание нового партнера
    @PostMapping
    public String createPartner(@ModelAttribute Partner partner, @RequestParam("confirmPassword") String confirmPassword, Model model) {
        if (!partner.getPassword().equals(confirmPassword)) {
            model.addAttribute("errorMessage", "Пароли не совпадают");
            return "partners/partner_form";
        }

        if (partner.getPassword().length() < 6) {
            model.addAttribute("errorMessage", "Пароль должен быть длиной не менее 6 символов");
            return "partners/partner_form";
        }

        partnerService.createPartner(partner);
        return "redirect:/home_partners";
    }

    // Удаление партнера
    @DeleteMapping("/{id}")
    public String deletePartner(@PathVariable Long id) {
        partnerService.deletePartner(id);
        return "redirect:/partners";
    }

    // Логаут партнера
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/exit_Account";
    }

    // Отображение формы редактирования партнера
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, HttpSession session) {
        Optional<Partner> partner = partnerService.getPartnerById(id);
        Partner loggedInPartner = (Partner) session.getAttribute("loggedInPartner");

        // Проверяем, авторизован ли партнер и имеет ли право редактировать
        if (partner.isPresent() && loggedInPartner != null && partner.get().getId().equals(loggedInPartner.getId())) {
            model.addAttribute("partner", partner.get());
            return "partners/edit_partner";
        } else {
            return "redirect:/partner_Account"; // Перенаправляем, если не авторизован
        }
    }
    // Отображение страницы редактирования профиля
    @GetMapping("/profile")
    public String showProfilePage(HttpSession session, Model model) {
        Partner loggedInPartner = (Partner) session.getAttribute("loggedInPartner");

        if (loggedInPartner != null) {
            model.addAttribute("partner", loggedInPartner);
            return "partners/edit_profile"; // шаблон для редактирования профиля
        } else {
            return "redirect:/exit_Account"; // перенаправление если партнер не залогинен
        }
    }

    // Обновление данных партнера
    @PostMapping("/update/{id}")
    public String updatePartner(@PathVariable Long id,
                                @ModelAttribute Partner updatedPartner,
                                @RequestParam(value = "newPassword", required = false) String newPassword,
                                @RequestParam(value = "confirmPassword", required = false) String confirmPassword,
                                HttpSession session,
                                Model model) {

        Optional<Partner> partnerOpt = partnerService.getPartnerById(id);
        Partner loggedInPartner = (Partner) session.getAttribute("loggedInPartner");

        // Проверка авторизации партнера
        if (partnerOpt.isPresent() && loggedInPartner != null && partnerOpt.get().getId().equals(loggedInPartner.getId())) {
            Partner existingPartner = partnerOpt.get();
            // Обновляем личные данные
            existingPartner.setFirstName(updatedPartner.getFirstName());
            existingPartner.setLastName(updatedPartner.getLastName());
            existingPartner.setEmail(updatedPartner.getEmail());
            existingPartner.setPhone(updatedPartner.getPhone());

            // Логика изменения пароля (если введен новый пароль)
            if (newPassword != null && !newPassword.isEmpty()) {
                if (!newPassword.equals(confirmPassword)) {
                    model.addAttribute("errorMessage", "Пароли не совпадают");
                    return "partners/edit_profile"; // возвращаемся к форме с ошибкой
                }
                if (newPassword.length() < 6) {
                    model.addAttribute("errorMessage", "Пароль должен быть длиной не менее 6 символов");
                    return "partners/edit_profile"; // возвращаемся к форме с ошибкой
                }
                existingPartner.setPassword(newPassword); // Обновляем пароль
            }
            // Сохраняем обновленные данные
            partnerService.createPartner(existingPartner);

            return "redirect:/partners/profile"; // Возвращаемся на страницу профиля после обновления
        }
        return "redirect:/partner_Account"; // Перенаправляем, если не авторизован
    }
    // Отображение главной страницы для партнеров
    @GetMapping("/home_partners")
    public String showHomePage(HttpSession session, Model model) {
        Partner loggedInPartner = (Partner) session.getAttribute("loggedInPartner");

        // Проверяем, авторизован ли партнер
        if (loggedInPartner != null) {
            model.addAttribute("partner", loggedInPartner); // Добавляем объект partner в модель
            model.addAttribute("welcomeMessage", "Вітаю, " + loggedInPartner.getFirstName() + "!");
            return "home_partners";
        } else {
            model.addAttribute("errorMessage", "Пожалуйста, войдите в систему.");
            return "redirect:/partner_Account"; // Перенаправляем, если партнер не авторизован
        }
    }

    // Обработка продолжения регистрации для незарегистрированного партнера
    @PostMapping("/continue_registration")
    public String continueRegistration(HttpSession session) {
        if (session.getAttribute("userName") == null) {
            return "redirect:/partners/new";
        } else {
            return "redirect:/home_partners";
        }
    }

    // Обработка перехода на добавление нового жилья
    @PostMapping("/add_hotels")
    public String addHotels(HttpSession session) {
        if (session.getAttribute("userName") != null) {
            return "redirect:/add_hotels";
        } else {
            return "redirect:/partners/login";
        }
    }

    // Обработка перехода на просмотр существующих отелей партнера
    @GetMapping("/hotels_by_partner")
    public String showHotelsByPartner(HttpSession session, Model model) {
        Partner loggedInPartner = (Partner) session.getAttribute("loggedInPartner");

        // Проверяем, авторизован ли партнер
        if (loggedInPartner == null) {
            return "redirect:/partner_Account"; // Перенаправляем на страницу логина, если партнер не авторизован
        }

        // Получаем отели, зарегистрированные партнером
        List<Hotel> hotels = hotelService.getHotelsByOwner(loggedInPartner);
        model.addAttribute("hotels", hotels);
        return "hotels_by_partner"; // Отображаем отели партнера
    }
}
