package com.example.mybooking.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MainController {
    @GetMapping("/")
    public String home(@RequestParam (name = "name", required = false, defaultValue = "Olga") String name, Model model ){
        model.addAttribute("namehtml", name);
        return "/home";
    }
//головна сторінка для партнера після реєстрації
    @GetMapping("/home_partners")
    public String home_partners( Model model ){
        model.addAttribute("home_partners");
        return "/home_partners";
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
//    @GetMapping("/registration")
//    public String registration(Model model ){
////       model.addAttribute("registration");
//        return "users/registration";
//    }

    @GetMapping("/support")
    public String support(Model model ){
//       model.addAttribute("support");
        return "/supports";
    }


}
