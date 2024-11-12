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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

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


            // Используем BCrypt для проверки введенного пароля с хешированным паролем в базе
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            if (passwordEncoder.matches(password, partner.getPassword())) {
                session.setAttribute("loggedInPartner", partner);
                session.setAttribute("userName", partner.getFirstName());
                return "redirect:/home_partners";
            } else {
                model.addAttribute("errorMessage", "Невірний email або пароль");
                return "partner_Account";
            }
        } else {
            model.addAttribute("errorMessage", "Партнер з таким email не знайдений.");
            return "partner_Account";
        }
    }
/////////////
    // Создание нового партнера
    @PostMapping
    public String createPartner(@ModelAttribute Partner partner, @RequestParam("confirmPassword") String confirmPassword, Model model) {
        if (!partner.getPassword().equals(confirmPassword)) {
            model.addAttribute("errorMessage", "Паролі не співпадають");
            return "partners/partner_form";
        }

        if (partner.getPassword().length() < 6) {
            model.addAttribute("errorMessage", "Пароль повинен бути не меньше 6 сімволів");
            return "partners/partner_form";
        }
        // Хешируем пароль перед сохранением
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String hashedPassword = passwordEncoder.encode(partner.getPassword());
        partner.setPassword(hashedPassword);

        partnerService.createPartner(partner);
        return "redirect:/home_partners";
    }

    // Удаление партнера
    @DeleteMapping("/{id}")
    public String deletePartner(@PathVariable Long id) {
        partnerService.deletePartner(id);
        return "redirect:/partners";
    }

    private Long getCurrentPartnerId() {
        // Логика получения ID текущего партнера из сессии
        return 1L; // Временно возвращаем ID партнера 1
    }

    // Логаут партнера
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/home_partners"; // Перенаправляем на home_partners (неавторизованный вариант)
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
    @GetMapping("/profile_Partner")
    public String showProfilePage(HttpSession session, Model model) {
        Partner loggedInPartner = (Partner) session.getAttribute("loggedInPartner");

        if (loggedInPartner != null) {
            System.out.println("Logged in partner: " + loggedInPartner.getFirstName()); // Отладочное сообщение
            model.addAttribute("partner", loggedInPartner);
//            model.addAttribute("welcomeMessage", "Вітаю, " + loggedInPartner.getFirstName() + "!");
            return "profile_Partner"; // шаблон для редактирования профиля
        } else {
            return "redirect:/exit_Account"; // перенаправление если партнер не залогинен
        }
    }
    // Обновление данных партнера в его личном кабинете
    @PostMapping("/update_profile/{id}")
    public String updateProfilePartner(@PathVariable Long id,
                                @ModelAttribute Partner updatedPartner,
                                @RequestParam(value = "newPassword", required = false) String newPassword,
                                @RequestParam(value = "confirmPassword", required = false) String confirmPassword,
                                HttpSession session,
                                Model model) {


        Partner loggedInPartner = (Partner) session.getAttribute("loggedInPartner");

        // Проверяем, авторизован ли пользователь и соответствует ли ID в запросе
        if (loggedInPartner != null && loggedInPartner.getId().equals(id)) {
            // Передаем обработку логики в сервис
            String errorMessage = partnerService.updatePartnerProfile(loggedInPartner, updatedPartner, newPassword, confirmPassword);

            if (errorMessage != null) {
                // Если возникла ошибка (например, пароли не совпадают), возвращаемся на страницу профиля с ошибкой
                model.addAttribute("errorMessage", errorMessage);
                //model.addAttribute("partner", loggedInPartner); // Добавляем партнера для Thymeleaf
                return "profile_Partner"; // Возврат на страницу профиля
            }

            // Обновляем объект в сессии после успешного сохранения
            session.setAttribute("loggedInPartner", loggedInPartner);
            return "redirect:/partners/profile_Partner"; // Перенаправляем на страницу профиля
        }

        return "redirect:/partner_Account"; // Перенаправляем, если пользователь не авторизован
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
                    model.addAttribute("errorMessage", "Паролі не співпадають");
                    return "partners/edit_profile"; // возвращаемся к форме с ошибкой
                }
                if (newPassword.length() < 6) {
                    model.addAttribute("errorMessage", "Пароль має бути не меньше 6 сімволів");
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

    @GetMapping("/home_partners")
    public String showHomePage(HttpSession session, Model model) {
        // Получаем объект Partner из сессии, если пользователь залогинен
        Partner loggedInPartner = (Partner) session.getAttribute("loggedInPartner");

        // Проверяем, авторизован ли партнер (проверяем, есть ли объект loggedInPartner в сессии)
        if (loggedInPartner != null) {
            // Если партнер авторизован, добавляем его данные в модель
            model.addAttribute("partner", loggedInPartner);
           // model.addAttribute("welcomeMessage", "Вітаю, " + loggedInPartner.getFirstName() + "!"); // Приветственное сообщение

            // Возвращаем представление (шаблон) "home_partners", который отображает страницу для авторизованных партнеров
            return "home_partners";
        }  else {
            // Если партнер не авторизован, завершить сессию
            session.invalidate();  // Завершаем текущую сессию
            return "home_partners"; // Возвращаем страницу для неавторизованных пользователей
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
    //////////////
    @GetMapping("/delete")
    public String deleteAccount(HttpSession session) {
        Partner loggedInPartner = (Partner) session.getAttribute("loggedInPartner");

        if (loggedInPartner != null) {
            partnerService.deletePartner(loggedInPartner.getId());
            session.invalidate(); // Завершаем сессию после удаления аккаунта
        }

        return "redirect:/"; // Перенаправляем на главную страницу после удаления
    }


}
