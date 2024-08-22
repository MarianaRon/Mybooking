package com.example.mybooking.controller;

import com.example.mybooking.model.User;
import com.example.mybooking.repository.IUserRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Controller
public class MainController {

    @Autowired
    private IUserRepository userRepository;

    @GetMapping("/")
    public String home(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser != null) {
            model.addAttribute("welcomeMessage", "Вітаємо, " + currentUser.getUsername() + "!");
        } else {
            model.addAttribute("welcomeMessage", "Вітаємо, гість!");
        }
        return "/home"; // головна сторінка
    }

    @GetMapping("/login")
    public String loginForm(Model model) {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, HttpSession session, Model model) {
        User user = userRepository.findByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            session.setAttribute("currentUser", user);
            return "redirect:/";
        }
        model.addAttribute("error", "Невірне ім'я користувача або пароль");
        return "login";
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



//головна сторінка для партнера після реєстрації
    @GetMapping("/home_partners")
    public String home_partners( Model model ){
        model.addAttribute("home_partners");
        return "/home_partners";
    }

    @GetMapping("/admin_page")
    public String admin_page( Model model ){
        model.addAttribute("admin_page");
        return "/admin_page";
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

//    @GetMapping("/currency")
//    public String currency(Model model ){
//        model.addAttribute("currency");
//        return "currency";
  //  }
//    @GetMapping("/login")
//    public String login(Model model ){
////       model.addAttribute("login");
//        return "users/login";
//    }
//

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


}
