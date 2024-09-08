package com.example.mybooking.controller;

import com.example.mybooking.model.User;
import com.example.mybooking.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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

//    для зміни паролю
@PostMapping("/change-password")
public String changePassword(@RequestParam("oldPassword") String oldPassword,
                             @RequestParam("confirmOldPassword") String confirmOldPassword,
                             @RequestParam("newPassword") String newPassword,
                             HttpSession session, Model model) {
    // Отримуємо поточного користувача із сесії
    User currentUser = (User) session.getAttribute("currentUser");
    if (currentUser == null) {
        return "redirect:/login"; // Якщо користувач не залогінений
    }
    // Перевіряємо, чи співпадають старі паролі
    if (!oldPassword.equals(confirmOldPassword)) {
        model.addAttribute("error", "Старі паролі не співпадають");
        return "redirect:/user_account"; // Повертаємося на сторінку користувача
    }
    // Перевірка, чи правильний старий пароль
    if (!userService.checkPassword(currentUser, oldPassword)) {
        model.addAttribute("error", "Неправильний старий пароль");
        return "redirect:/user_account"; // Повертаємося на сторінку користувача
    }
    // Змінюємо пароль
    userService.changePassword(currentUser, newPassword);
    // Після успішної зміни паролю передаємо параметр для показу успішного модального вікна
    model.addAttribute("success", true);
    return "redirect:/user_account"; // Повертаємося на сторінку користувача
}

    // Метод для редагування поточного користувача з Кабінету користувача
    @GetMapping("/edit_user_account")
    public String editUserAccountForm(HttpSession session, Model model) {
        // Отримуємо поточного користувача з сесії
        User currentUser = (User) session.getAttribute("currentUser");

        if (currentUser == null) {
            return "redirect:/login"; // Якщо користувач не залогінений, переходимо на сторінку входу
        }

        // Додаємо поточного користувача в модель для форми редагування
        model.addAttribute("user", currentUser);
        return "users/edit_user_account"; // Повертаємо на сторінку редагування профілю
    }

    // Метод для обробки POST запиту на оновлення даних користувача
    @PostMapping("/edit_user_account")
    public String updateUserAccount(@ModelAttribute User user, HttpSession session) {
        // Отримуємо поточного користувача з сесії
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login"; // Якщо користувач не залогінений
        }
        // Якщо поле з паролем порожнє, використовуємо старий пароль
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            user.setPassword(currentUser.getPassword());
        }
        // Оновлюємо дані користувача в базі
        user.setId(currentUser.getId());
        userService.saveUser(user);
        // Оновлюємо сесію з новими даними користувача
        session.setAttribute("currentUser", user);
        return "redirect:/user_account"; // Повертаємося на сторінку Кабінету користувача
    }


}
