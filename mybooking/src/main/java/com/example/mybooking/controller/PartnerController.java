package com.example.mybooking.controller;

import com.example.mybooking.model.Partner;
import com.example.mybooking.service.HotelService;
import com.example.mybooking.service.PartnerService;
import com.example.mybooking.service.UserService;

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
        Optional<Partner> partner = partnerService.findByEmail(email);

        if (partner.isPresent() && partner.get().getPassword().equals(password)) {
            session.setAttribute("loggedInPartner", partner.get());  // Сохраняем партнера в сессии
            session.setAttribute("userName", partner.get().getFirstName());  // Сохраняем имя пользователя для приветствия
            return "redirect:/home_partners";
        } else {
            model.addAttribute("errorMessage", "Invalid email or password");
            return "partners/partner_login";
        }
    }
    @PostMapping
    public String createPartner(
            @ModelAttribute Partner partner,
            @RequestParam("confirmPassword") String confirmPassword,
            Model model) {

        if (!partner.getPassword().equals(confirmPassword)) {
            model.addAttribute("errorMessage", "Passwords do not match");
            return "partners/partner_form";
        }

        if (partner.getPassword().length() < 6) {
            model.addAttribute("errorMessage", "Password must be at least 6 characters long");
            return "partners/partner_form";
        }

        partnerService.createPartner(partner);
        return "redirect:/home_partners";
    }

    @DeleteMapping("/{id}")
    public String deletePartner(@PathVariable Long id) {
        partnerService.deletePartner(id);
        return "redirect:/partners";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/exit_Account";
    }
    // Отображение формы редактирования партнера
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Optional<Partner> partner = partnerService.getPartnerById(id);
        if (partner.isPresent()) {
            model.addAttribute("partner", partner.get());
            return "partners/edit_partner";
        } else {
            return "redirect:/partners";
        }
    }

    @PostMapping("/update/{id}")
    public String updatePartner(@PathVariable Long id, @ModelAttribute Partner updatedPartner, Model model) {
        Optional<Partner> partner = partnerService.getPartnerById(id);
        if (partner.isPresent()) {
            Partner existingPartner = partner.get();
            existingPartner.setEmail(updatedPartner.getEmail());
            existingPartner.setFirstName(updatedPartner.getFirstName());
            existingPartner.setLastName(updatedPartner.getLastName());
            existingPartner.setPhone(updatedPartner.getPhone());

            if (updatedPartner.getPassword().length() >= 6) {
                existingPartner.setPassword(updatedPartner.getPassword());
            } else {
                model.addAttribute("errorMessage", "Password must be at least 6 characters long");
                return "partners/edit_partner";
            }

            partnerService.createPartner(existingPartner);
        }
        return "redirect:/partners";
    }

    // Отображение главной страницы для партнеров
    @GetMapping("/home_partners")
    public String showHomePage(HttpSession session, Model model) {
        String userName = (String) session.getAttribute("userName");

        // Если пользователь залогинен, показываем главную страницу для партнера
        if (userName != null) {
            model.addAttribute("welcomeMessage", "Вітаю, " + userName + "!");
            return "home_partners";
        } else {
            // Если пользователь не залогинен, перенаправляем на страницу входа
            return "redirect:/partner_Account";
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
    public String showHotelsByPartner(HttpSession session) {
        if (session.getAttribute("userName") != null) {
            return "hotels_by_partner"; // или другой шаблон, связанный с отображением отелей
        } else {
            return "redirect:/partners/login";
        }
    }
}
