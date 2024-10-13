package com.example.mybooking.controller;

import com.example.mybooking.model.*;
import com.example.mybooking.repository.IUserRepository;
import com.example.mybooking.service.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.stream.Collectors;

@Controller
public class MainController {

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private CityService cityService;
    @Autowired
    private UserService userService;
    @Autowired
    private UserMessageService userMessageService;
    @Autowired
    private HotelService hotelService;
    @Autowired
    private PartnerService partnerService;
    @Autowired
    private ReviewService reviewService;
    @Autowired
    private ReservationService reservationService;
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    @GetMapping("/")
    public String home(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        List<City> cities = cityService.getAllCities();
        model.addAttribute("cities", cities);
        List<Hotel> hotels = hotelService.get_AllHotels();
        model.addAttribute("hotels", hotels);
        // Визначаємо топ-3 готелі за рейтингом
        List<Hotel> topHotels = hotels.stream()
                .sorted((h1, h2) -> Double.compare(
                        h2.getReviews().stream().mapToDouble(Review::getRating).average().orElse(0.0),
                        h1.getReviews().stream().mapToDouble(Review::getRating).average().orElse(0.0)
                ))
                .limit(3)
                .toList();

        model.addAttribute("topHotels", topHotels);  // Додаємо топ-3 готелі до моделі
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
            return "redirect:/login";
        }
        List<Review> reviews = reviewService.findReviewsByUser(currentUser);
        List<Reservation> reservations = reservationService.findReservationsByUser(currentUser);

        model.addAttribute("user", currentUser);
        model.addAttribute("reviews", reviews);
        model.addAttribute("reservations", reservations);

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
    //форма для додавання номеру партнером (перехід з add_hotel)
    @GetMapping("/add_room")
    public String add_room(Model model ){
        model.addAttribute("add_room");
        return "add_room";
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

//    @GetMapping("/hotels/hotel_list")
//    public String hotel_list(Model model ){
//        List<City> cities = cityService.getAllCities();
//        model.addAttribute("cities", cities);
//        return "/hotels/hotel_list";
//    }
//    @GetMapping("/hotels/hotel_list")
//    public String hotelList(Model model) {
//        List<Hotel> hotels = hotelService.getAllHotels();  // Отримуємо список готелів
//        List<City> cities = cityService.getAllCities();    // Отримуємо список міст
//        List<Partner> partners = partnerService.getAllPartners();
//        model.addAttribute("hotels", hotels);  // Додаємо готелі до моделі
//        model.addAttribute("cities", cities);  // Додаємо міста до моделі
//        model.addAttribute("partners", partners);
//        return "hotels/hotel_list";  // Назва шаблону для списку готелів
//    }

//    @GetMapping("/about_us")
//    public String about_us(Model model ){
//        return "/about_us";
//    }

//    @GetMapping("/about_us")
//    public String aboutUs(Model model) {
//        // Отримуємо всі відгуки
//        List<Review> allReviews = reviewService.getAllReviews();
//
//        // Перша трійка відгуків для початкового відображення
//        List<Review> firstThreeReviews = allReviews.subList(0, Math.min(3, allReviews.size()));
//
//        // Додаємо відгуки в модель для відображення в каруселі
//        model.addAttribute("reviews", firstThreeReviews);
//        model.addAttribute("totalReviews", allReviews.size());
//
//        // Повертаємо сторінку "about_us"
//        return "about_us";
//    }

    @GetMapping("/about_us")
    public String aboutUs(Model model) {
        // Отримуємо всі відгуки
        List<Review> allReviews = reviewService.getAllReviews();

        // Додаємо всі відгуки в модель для каруселі
        model.addAttribute("reviews", allReviews);
        model.addAttribute("totalReviews", allReviews.size());

        // Повертаємо сторінку "about_us"
        return "about_us";
    }


    @GetMapping("/admin_message_list")
    public String viewMessages(Model model) {
        model.addAttribute("messages", userMessageService.getAllMessages());
        return "admin_message_list"; // Сторінка з повідомленнями
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


    @GetMapping("/all_hotels")
    public String showHotels(Model model) {
        List<Hotel> hotels = hotelService.getAllHotels();
        model.addAttribute("hotels", hotels);  // Додаємо список готелів у модель
        return "hotels";  // Повертаємо назву шаблону
    }


}
