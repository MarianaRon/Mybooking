package com.example.mybooking.controller;

import com.example.mybooking.model.City;
import com.example.mybooking.model.User;
import com.example.mybooking.repository.IUserRepository;
import com.example.mybooking.service.UserService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.example.mybooking.service.CityService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Principal;
import java.util.Collections;
import java.util.List;
@Controller
public class MainController {

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private CityService cityService;
    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String home(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        List<City> cities = cityService.getAllCities();  // Отримуємо список міст
        model.addAttribute("cities", cities);  // Додаємо міста в модель

        if (currentUser != null) {
            model.addAttribute("welcomeMessage", "Вітаємо, " + currentUser.getUsername() + "! Спробуйте найпопулярніші напрямки для подорожі");
        } else {
            model.addAttribute("welcomeMessage", "Вітаємо, гість! Спробуйте найпопулярніші напрямки для подорожі");
        }
        return "home";  
    }
    @GetMapping("/login")
    public String loginForm(Model model) {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, HttpSession session, Model model) {
        User user = userRepository.findByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            session.setAttribute("currentUser", user); // Store user in session
            return "redirect:/";
        }
        model.addAttribute("error", "Невірне ім'я користувача або пароль");
        return "login";
    }

    @GetMapping("/user_account")
    public String userAccount(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login"; // Якщо користувач не залогінений, перенаправити на сторінку входу
        }
        model.addAttribute("user", currentUser); // Передати користувача в модель
        return "users/user_account"; // Показати сторінку кабінету користувача
    }



    @PostMapping("/google-login")
    public String googleLogin(@RequestParam String idTokenString, HttpSession session) throws GeneralSecurityException, IOException {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new JacksonFactory())
                .setAudience(Collections.singletonList("YOUR_GOOGLE_CLIENT_ID"))
                .build();

        GoogleIdToken idToken = verifier.verify(idTokenString);
        if (idToken != null) {
            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String name = (String) payload.get("name");

            // Знайдіть або створіть користувача в базі даних за email
            User user = userRepository.findByEmail(email);
            if (user == null) {
                user = new User();
                user.setEmail(email);
                user.setUsername(name);
                userRepository.save(user);
            }
            session.setAttribute("currentUser", user);
            return "redirect:/";
        } else {
            return "redirect:/login?error";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }


//нове додано для реєстрації
@PostMapping("/registration")
public String registerUser(@ModelAttribute("user") User user, HttpSession session, Model model) {
    // Перевірка, чи користувач з таким email або username вже існує
    if (userRepository.findByEmail(user.getEmail()) != null || userRepository.findByUsername(user.getUsername()) != null) {
        model.addAttribute("error", "Користувач з таким ім'ям або електронною адресою вже існує");
        return "registration"; // Повертаємося на сторінку реєстрації з повідомленням про помилку
    }

    // Збереження нового користувача
    userRepository.save(user);
    session.setAttribute("currentUser", user);
    return "redirect:/"; // Переходимо на сторінку входу після успішної реєстрації
}


    //головна сторінка для партнера після реєстрації
    @GetMapping("/home_partners")
    public String home_partners( Model model ){
        model.addAttribute("home_partners");
        return "/home_partners";
    }

    @GetMapping("/admin_page")
    public String adminPage(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser != null && "okobilska@gmail.com".equals(currentUser.getEmail())) {
            return "/admin_page"; // Proceed to the admin page
        }
        return "redirect:/"; // Redirect to home if not authorized
    }


    @GetMapping("/contacts")
    public String contacts( Model model ){
        model.addAttribute("contacts");
        return "/contacts";
    }
//
    @GetMapping("/hotel_search")
    public String hotel_search(Model model ){
   model.addAttribute("hotel_search");
        return "hotels/hotel_search";
    }
    //форма для реєстрації помешкання
    @GetMapping("/hotel_registration")
    public String  hotel_registration(Model model ){
     model.addAttribute("hotel_registration");
        return "hotels/hotel_registration";
    }
    //форма для реєстрації акаунта партнера
    @GetMapping("/partner_Account")
    public String partner_Account(Model model ){
        model.addAttribute("partner_Account");
        return "partner_Account";
    }
    //форма для входу в акаунт партнера
    @GetMapping("/exit_Account")
    public String exit_Account(Model model ){
        model.addAttribute("exit_Account");
        return "exit_Account";
    }
//Переглянути мої помешкання (перехід з home_partners)
    @GetMapping("/hotels_by_partner")
    public String hotels_by_partner(Model model ){
        model.addAttribute("hotels_by_partner");
        return "hotels_by_partner";
    }
    //форма для додавання помешкання партнером (перехід з home_partners)
    @GetMapping("/add_hotels")
    public String add_hotels(Model model ){
        model.addAttribute("add_hotels");
        return "add_hotels";
    }

    @GetMapping("/registration")
    public String registrationForm(Model model) {
        model.addAttribute("user", new User());
        return "registration";
    }

    @GetMapping("/support")
    public String support(Model model ){
//       model.addAttribute("support");
        return "/supports";
    }

    @GetMapping("/hotels/hotel_list")
    public String hotel_list(Model model ){
        return "/hotels/hotel_list";
    }

    @GetMapping("/about_us")
    public String about_us(Model model ){
        return "/about_us";
    }

    //контролер для відправки підписки на новини
    @PostMapping("/subscribe")
    public String subscribeToNewsletter(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            model.addAttribute("errorNewsletter", true); // Показати червоне повідомлення
            return "redirect:/?subscriptionError=true"; // Додаємо параметр в URL для індикації помилки
        }

        if (!currentUser.isSubscribedToNewsletter()) {
            userService.subscribeUser(currentUser);
            return "redirect:/?subscriptionSuccess=true"; // Додаємо параметр в URL для успішної підписки
        }
        return "redirect:/";
    }

    @GetMapping("/admin_subscribers")
    public String showAdminPage(Model model) {
        List<String> subscribers = userService.getAllSubscribers();
        System.out.println("Subscribers: " + subscribers); // Логування списку
        model.addAttribute("subscribers", subscribers);
        return "admin_page";
    }
}
