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


    @GetMapping
    public List<Partner> getAllPartners() {
        return partnerService.getAllPartners();
    }

    @GetMapping("/{id}")
    public Optional<Partner> getPartnerById(@PathVariable Long id) {
        return partnerService.getPartnerById(id);
    }

    @GetMapping("/new")
    public String showPartnerForm() {
        return "partner_Account";
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "exit_Account";
    }

    @PostMapping("/login")
    public String loginPartner(@RequestParam String email, @RequestParam String password, HttpSession session, Model model) {
        Optional<Partner> partner = partnerService.findByEmail(email);

        if (partner.isPresent() && partner.get().getPassword().equals(password)) {
            session.setAttribute("partnerEmail", partner.get().getEmail());
            session.setAttribute("userName", partner.get().getFirstName());
            return "redirect:/home_partners";
        } else {
            model.addAttribute("errorMessage", "Invalid email or password");
            return "partner_login";
        }
    }

    @PostMapping
    public String createPartner(
            @ModelAttribute Partner partner,
            @RequestParam("confirmPassword") String confirmPassword,
            Model model) {

        if (!partner.getPassword().equals(confirmPassword)) {
            model.addAttribute("errorMessage", "Passwords do not match");
            return "partner_Account";
        }

        if (partner.getPassword().length() < 6) {
            model.addAttribute("errorMessage", "Password must be at least 6 characters long");
            return "partner_Account";
        }

        partnerService.createPartner(partner);
        return "redirect:/home_partners";
    }

    @DeleteMapping("/{id}")
    public String deletePartner(@PathVariable Long id) {
        partnerService.deletePartner(id);
        return "redirect:/partners/partner_list";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/partners/login";
    }

    @GetMapping("/partner_list")
    public String partnerList(Model model) {
        List<Partner> partners = partnerService.getAllPartners();
        model.addAttribute("partners", partners);
        return "partners/partner_list";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Optional<Partner> partner = partnerService.getPartnerById(id);
        if (partner.isPresent()) {
            model.addAttribute("partner", partner.get());
            return "partners/edit_partner";
        } else {
            return "redirect:/partners/partner_list";
        }
    }

    @PostMapping("/update/{id}")
    public String updatePartner(@PathVariable Long id, @ModelAttribute Partner updatedPartner) {
        Optional<Partner> partner = partnerService.getPartnerById(id);
        if (partner.isPresent()) {
            Partner existingPartner = partner.get();
            existingPartner.setEmail(updatedPartner.getEmail());
            existingPartner.setFirstName(updatedPartner.getFirstName());
            existingPartner.setLastName(updatedPartner.getLastName());
            existingPartner.setPhone(updatedPartner.getPhone());
            if (updatedPartner.getPassword().length() >= 6) {
                existingPartner.setPassword(updatedPartner.getPassword());
            }
            partnerService.createPartner(existingPartner);
        }
        return "redirect:/partners/partner_list";
    }
}
