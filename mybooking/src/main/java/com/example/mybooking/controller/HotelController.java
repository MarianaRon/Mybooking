package com.example.mybooking.controller;

import com.example.mybooking.model.Hotel;
import com.example.mybooking.model.Partner;
import com.example.mybooking.model.User;
import com.example.mybooking.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/hotels")
public class HotelController {

    @Autowired
    private HotelService hotelService;

    @Autowired
    private PartnerService partnerService;

    @Autowired
    private RoomService roomService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private AmenityService amenityService;

    @Autowired
    private ImageService imageService;
    private Partner ownerPartner;

    // Отримання всіх готелів
    @GetMapping
    public String getAllHotels(Model model) {
        List<Hotel> hotels = hotelService.getAllHotels();
        model.addAttribute("hotels", hotels);
        return "hotels/hotel_list";
    }
    // Отримання готелю за ID
    @GetMapping("/{id}")
    public String getHotelById(@PathVariable Long id, Model model)
    {
        Optional<Hotel> hotel = hotelService.getHotelById(id);
        if (hotel.isPresent()) {
            model.addAttribute("hotel", hotel.get());
            return "hotels/hotel_details";
        } else {
            return "redirect:/hotels";
        }
    }
    // Форма для добавления отеля
    @GetMapping("/add")
    public String showAddHotelForm(HttpSession session, Model model) {
        Hotel hotel = (Hotel) session.getAttribute("hotel");
        if (hotel == null) {
            hotel = new Hotel();
            session.setAttribute("hotel", hotel);
        }
        model.addAttribute("hotel", hotel);

        List<Partner> partners = partnerService.getAllPartners();
        model.addAttribute("partners", partners);

        return "add_hotels";
    }
    @PostMapping("/add")
    public String processFormStep(@ModelAttribute Hotel hotel, HttpSession session, @RequestParam("step") int step) {
        Hotel sessionHotel = (Hotel) session.getAttribute("hotel");
        if (sessionHotel == null) {
            sessionHotel = new Hotel();
            session.setAttribute("hotel", sessionHotel);
        }

        // Обновляем объект Hotel в сессии в зависимости от шага
        switch (step) {
            case 1:
                sessionHotel.setName(hotel.getName());
                break;
            case 2:

                sessionHotel.setAddressCity(hotel.getAddressCity());
                sessionHotel.setAddressStreet(hotel.getAddressStreet());
                sessionHotel.setAdditionalInfo(hotel.getAdditionalInfo());
                sessionHotel.setLatitude(hotel.getLatitude());
                sessionHotel.setLongitude(hotel.getLongitude());
                break;
            case 3:
                sessionHotel.setAmenities(hotel.getAmenities());
                sessionHotel.setPrice(hotel.getPrice());
                break;
            case 4:
                sessionHotel.setDescription(hotel.getDescription());
                // Здесь вы можете обработать загрузку файла изображения
                break;
        }

        // Перенаправляем на следующий шаг формы
        return "redirect:/hotels/add?step=" + (step + 1);
    }
    @PostMapping("/submit")
    public String submitHotel(HttpSession session) {
        Hotel hotel = (Hotel) session.getAttribute("hotel");
        if (hotel != null) {
            hotelService.saveHotel(hotel);
            session.removeAttribute("hotel"); // Удаляем данные из сессии после сохранения
        }
        return "redirect:/hotels/by-partner";
    }

    // Отримання готелів, зареєстрованих партнером
    @GetMapping("/by-partner")
    public String getHotelsByPartner(@SessionAttribute("loggedInPartner") Partner loggedInPartner, Model model) {
        // Передаем в модель отели, принадлежащие партнеру
        model.addAttribute("hotels", hotelService.getHotelsByOwner(loggedInPartner));
        return "hotels_by_partner";
    }
    // Показ форми реєстрації готелю
    @GetMapping("/hotel_registration")
    public String showRegistrationForm(Model model) {
        model.addAttribute("hotel", new Hotel());
        model.addAttribute("partners", partnerService.getAllPartners());
        return "hotel_form";
    }


    @GetMapping("/edit/{id}")
    public String editHotel(@PathVariable Long id, Model model) {
        // Получаем отель по ID или выбрасываем исключение, если отель не найден
        Hotel hotel = hotelService.getHotelById(id)
                .orElseThrow(() -> new RuntimeException("Hotel not found"));
        model.addAttribute("hotel", hotel);

        // Получаем список всех партнеров для выбора владельца отеля
        model.addAttribute("partners", partnerService.getAllPartners());

        return "edit_hotel";
    }

    @PostMapping("/edit/{id}")
    public String updateHotel(@PathVariable Long id,
                              @RequestParam String name,
                              @RequestParam String description,
                              @RequestParam String address,
                              @RequestParam(required = false) Double latitude,
                              @RequestParam(required = false) Double longitude,
                              @RequestParam Long owner, // Этот параметр теперь должен быть ID партнера
                              @RequestParam String housingType) {

        // Получаем отель по ID или выбрасываем исключение, если отель не найден
        Hotel hotel = hotelService.getHotelById(id)
                .orElseThrow(() -> new RuntimeException("Hotel not found"));

        // Получаем партнера по ID или выбрасываем исключение, если партнер не найден
        Partner ownerPartner = partnerService.getPartnerById(owner)
                .orElseThrow(() -> new IllegalArgumentException("Invalid owner ID"));

        // Обновляем информацию о отеле
        hotel.setName(name);
        hotel.setDescription(description);
        hotel.setAddress(address);
        hotel.setLatitude(latitude);
        hotel.setLongitude(longitude);
        hotel.setOwner(ownerPartner); // Устанавливаем партнера как владельца
        hotel.setHousingType(housingType);

        // Сохраняем изменения в базе данных
        hotelService.saveHotel(hotel);

        // Перенаправляем на страницу списка отелей
        return "redirect:/hotels/hotel_list";
    }
    @PostMapping("/delete/{id}")
    public String deleteHotel(@PathVariable Long id) {
        hotelService.deleteHotel(id);
        return "redirect:/hotels/hotel_list";
    }

    // Показ форми пошуку готелів
    @GetMapping("/search")
    public String showSearchForm(Model model) {
        model.addAttribute("searchTerm", "");
        return "search_form";
    }

    // Пошук готелів за назвою або описом
    @PostMapping("/search")
    public String searchHotels(@RequestParam String searchTerm, Model model) {
        List<Hotel> results = hotelService.searchHotelsByNameOrDescription(searchTerm);
        model.addAttribute("results", results);
        return "search_results";
    }
    @PostMapping("/hotels/register")
    public String registerHotel(@ModelAttribute Hotel hotel, BindingResult result) {
        if (result.hasErrors()) {
            return "hotel_registration_form"; // верните страницу с формой, если есть ошибки
        }
        hotelService.saveHotel(hotel);
        return "redirect:/hotels_by_partner"; // перенаправление после успешной регистрации
    }
}
