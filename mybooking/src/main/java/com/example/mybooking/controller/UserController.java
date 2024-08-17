package com.example.mybooking.controller;

import com.example.mybooking.model.User;
import com.example.mybooking.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/user_list")
    public String listUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "users/user_list";
    }

    @GetMapping("/edit/{id}")
    public String editUserForm(@PathVariable Long id, Model model) {
        User user = userService.getUserById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user Id: " + id));
        model.addAttribute("user", user);
        return "users/edit_user";
    }

    @PostMapping("/edit/{id}")
    public String updateUser(@PathVariable Long id, @ModelAttribute User user) {
        user.setId(id);
        userService.saveUser(user);
        return "redirect:/users/user_list";
    }

    @PostMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "redirect:/users/user_list";
    }

    // Новий метод для додавання користувача
    @PostMapping("/add")
    public String addUser(@RequestParam String username,
                          @RequestParam String password,
                          @RequestParam String email,
                          @RequestParam String firstName,
                          @RequestParam String lastName) {
        User user = new User(username, password, email, firstName, lastName);
        userService.saveUser(user);
        return "redirect:/users/user_list";
    }
}


//package com.example.mybooking.controller;
//
////
////package com.example.booking.controllers;
//
//import com.example.mybooking.model.User;
//import com.example.mybooking.repository.IUserRepository;
//import com.example.mybooking.service.UserService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//
//import jakarta.servlet.http.Cookie;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import jakarta.servlet.http.HttpSession;
//
//import java.util.Optional;
//
//@Controller
//@RequestMapping("/users")
//public class UserController {
//
//    @Autowired
//    private IUserRepository userRepository;
//    private UserService userService;
//
//    @PostMapping("/registration")
//    public String registerUser(@ModelAttribute User user, HttpServletResponse response, HttpSession session) {
//        userRepository.save(user);
//        Cookie cookie = new Cookie("userEmail", user.getEmail());
//        cookie.setMaxAge(60 * 60); // 1 hour
//        response.addCookie(cookie);
//        session.setAttribute("currentUser", user); // Сохранение пользователя в сессии
//        return "redirect:/";
//    }
//
//    @GetMapping("/logout")
//    public String logout(HttpServletRequest request, HttpServletResponse response) {
//        Cookie[] cookies = request.getCookies();
//        if (cookies != null) {
//            for (Cookie cookie : cookies) {
//                cookie.setMaxAge(0);
//                response.addCookie(cookie);
//            }
//        }
//        request.getSession().invalidate(); // Инвалидируем сессию
//        return "redirect:/";
//    }
//
////    @PostMapping("/logout")
////    public String logoutPost(HttpServletRequest request, HttpServletResponse response) {
////        Cookie[] cookies = request.getCookies();
////        if (cookies != null) {
////            for (Cookie cookie : cookies) {
////                cookie.setMaxAge(0);
////                response.addCookie(cookie);
////            }
////        }
////        request.getSession().invalidate(); // Инвалидируем сессию
////        return "redirect:/";
////    }
//
//    @GetMapping("/user_list")
//    public String listUsers(Model model) {
//        model.addAttribute("users", userRepository.findAll());
//        return "users/user_list";
//    }
//
//    @GetMapping("/edit/{id}")
//    public String editUserForm(@PathVariable Long id, Model model) {
////        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));
//        Optional<User> user = Optional.of(userService.getUserById(id).orElseThrow());
//        model.addAttribute("user", user);
//        return "users/edit_user";
//    }
//
//    @PostMapping("/edit/{id}")
//    public String updateUser(@PathVariable Long id, @ModelAttribute User user) {
//        user.setId(id);
//        userRepository.save(user);
//        return "redirect:/users/user_list";
//    }
//
//    @PostMapping("/delete/{id}")
//    public String deleteUser(@PathVariable Long id) {
//        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));
//        userRepository.delete(user);
//        return "redirect:/users/user_list";
//    }
//}
//
//
////
////import com.example.mybooking.model.User;
////import com.example.mybooking.service.UserService;
////import org.springframework.beans.factory.annotation.Autowired;
////import org.springframework.http.ResponseEntity;
////import org.springframework.web.bind.annotation.*;
////
////import java.util.List;
////import java.util.Optional;
////
////@RestController
////@RequestMapping("/users")
////public class UserController {
////
////    @Autowired
////    private UserService userService;
////
////    @GetMapping
////    public List<User> getAllUsers() {
////        return userService.getAllUsers();
////    }
////
////    @GetMapping("/{id}")
////    public ResponseEntity<User> getUserById(@PathVariable Long id) {
////        Optional<User> user = userService.getUserById(id);
////        if (user.isPresent()) {
////            return ResponseEntity.ok(user.get());
////        } else {
////            return ResponseEntity.notFound().build();
////        }
////    }
////
////    @PostMapping
////    public User createUser(@RequestBody User user) {
////        return userService.saveUser(user);
////    }
////
////    @PutMapping("/{id}")
////    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
////        Optional<User> optionalUser = userService.getUserById(id);
////        if (optionalUser.isPresent()) {
////            User user = optionalUser.get();
////            user.setUsername(userDetails.getUsername());
////            user.setPassword(userDetails.getPassword());
////            user.setEmail(userDetails.getEmail());
////            user.setFirstName(userDetails.getFirstName());
////            user.setLastName(userDetails.getLastName());
////            return ResponseEntity.ok(userService.saveUser(user));
////        } else {
////            return ResponseEntity.notFound().build();
////        }
////    }
////
////    @DeleteMapping("/{id}")
////    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
////        userService.deleteUser(id);
////        return ResponseEntity.noContent().build();
////    }
////}