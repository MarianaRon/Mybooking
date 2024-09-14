package com.example.mybooking.controller;

import com.example.mybooking.model.Hotel;
import com.example.mybooking.model.Partner;
import com.example.mybooking.service.*;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/hotels")
public class HotelController {
    private static final Logger logger = LoggerFactory.getLogger(HotelController.class);

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

    // Получение всех отелей
    @GetMapping
    public String getAllHotels(Model model) {
        List<Hotel> hotels = hotelService.getAllHotels();
        model.addAttribute("hotels", hotels);
        return "hotels/hotel_list";
    }
    // Получение отеля по ID
    @GetMapping("/hotel/{id}")
    public String getHotelById(@PathVariable Long id, Model model) {
        Optional<Hotel> hotel = hotelService.getHotelById(id);
        if (hotel.isPresent()) {
            model.addAttribute("hotel", hotel.get());
            return "hotels/hotel_details";
        } else {
            return "redirect:/hotels";
        }
    }

    // Показ формы для добавления отеля
    @GetMapping("/add")
    public String showAddHotelForm(HttpSession session, Model model, HttpServletResponse response) {
        Hotel hotel = new Hotel();
        model.addAttribute("hotel", hotel);

        // Отключаем кэширование
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1
        response.setHeader("Pragma", "no-cache"); // HTTP 1.0
        response.setDateHeader("Expires", 0); // Прокси

        // Получаем текущего авторизованного партнера
        Partner loggedInPartner = (Partner) session.getAttribute("loggedInPartner");
        if (loggedInPartner == null) {
            return "redirect:/partner_Account"; // Если партнер не авторизован
        }

        return "add_hotels"; // Возвращаем шаблон страницы добавления отеля
    }

    @PostMapping("/add")
    public String processFormStep(@ModelAttribute Hotel hotel, HttpSession session, @RequestParam("step") int step) {
        // Получаем объект отеля из сессии
        Hotel sessionHotel = (Hotel) session.getAttribute("hotel");

        // Если объекта отеля нет в сессии, создаем новый и сохраняем его
        if (sessionHotel == null) {
            sessionHotel = new Hotel();
            session.setAttribute("hotel", sessionHotel);
            logger.info("New hotel object created and saved in session");
        }

        // Логируем текущий шаг
        logger.info("Processing step {}: Current hotel state: {}", step, sessionHotel);

        // Проверка, есть ли в сессии авторизованный партнер
        Partner loggedInPartner = (Partner) session.getAttribute("loggedInPartner");
        if (loggedInPartner == null) {
            logger.error("Partner not found in session. Redirecting to login.");
            return "redirect:/partner_Account"; // Если партнер не авторизован
        }

        sessionHotel.setOwner(loggedInPartner); // Привязываем партнера к отелю

        // Обрабатываем обновление данных отеля в зависимости от шага
        switch (step) {
            case 1:
                if (hotel.getName() == null || hotel.getName().isEmpty()) {
                    logger.error("Hotel name is missing at step 1");
                    return "redirect:/hotels/add?step=1&error=name";
                }
                sessionHotel.setName(hotel.getName());
                break;
            case 2:
                if (hotel.getAddressCity() == null || hotel.getAddressStreet() == null) {
                    logger.error("Address data is incomplete at step 2");
                    return "redirect:/hotels/add?step=2&error=address";
                }
                sessionHotel.setAddressCity(hotel.getAddressCity());
                sessionHotel.setAddressStreet(hotel.getAddressStreet());
                sessionHotel.setLatitude(hotel.getLatitude());
                sessionHotel.setLongitude(hotel.getLongitude());
                break;
            case 3:
                if (hotel.getPrice() == null || hotel.getPrice() <= 0) {
                    logger.error("Price is missing or invalid at step 3");
                    return "redirect:/hotels/add?step=3&error=price";
                }
                sessionHotel.setPrice(hotel.getPrice());
                sessionHotel.setAmenities(hotel.getAmenities());
                break;
            case 4:
                if (hotel.getDescription() == null || hotel.getDescription().isEmpty()) {
                    logger.error("Description is missing at step 4");
                    return "redirect:/hotels/add?step=4&error=description";
                }
                sessionHotel.setDescription(hotel.getDescription());
                break;
            default:
                logger.error("Invalid step: {}", step);
                return "redirect:/hotels/add";
        }

        // Сохраняем объект отеля в сессии после каждого шага
        session.setAttribute("hotel", sessionHotel);
        logger.info("Hotel object successfully updated and saved in session after step {}", step);

        // Переход на следующий шаг
        return "redirect:/hotels/add?step=" + (step + 1);
    }
    @PostMapping("/submit")
    public String submitHotel(HttpSession session) {
        // Проверяем наличие объекта отеля в сессии
        Hotel hotel = (Hotel) session.getAttribute("hotel");

        if (hotel == null) {
            logger.error("Hotel not found in session during submit. Redirecting back to form.");
            return "redirect:/hotels/add?error=hotel_not_found";
        }

        // Проверяем наличие авторизованного партнера
        Partner loggedInPartner = (Partner) session.getAttribute("loggedInPartner");
        if (loggedInPartner == null) {
            logger.error("Partner not found in session during submit. Redirecting to login.");
            return "redirect:/partner_Account";
        }

        // Логируем данные перед сохранением
        logger.info("Submitting hotel for partner {}: {}", loggedInPartner.getId(), hotel);

        try {
            // Сохраняем отель в базе данных
            hotelService.saveHotelWithPartner(hotel, loggedInPartner);
            logger.info("Hotel successfully saved in the database with partner: {}", loggedInPartner.getId());

            // Удаляем объект отеля из сессии после успешного сохранения
            session.removeAttribute("hotel");
            logger.info("Hotel object removed from session after successful save.");
        } catch (Exception e) {
            logger.error("Error while saving hotel: {}", e.getMessage(), e);
            return "redirect:/hotels/add?error=save_failed";
        }

        return "redirect:/hotels_by_partner";
    }
    @GetMapping("/hotels_by_partner")
    public String getHotelsByPartner(HttpSession session, Model model) {
        // Получаем партнера из сессии
        Partner loggedInPartner = (Partner) session.getAttribute("loggedInPartner");

        if (loggedInPartner == null) {
            return "redirect:/partner_Account"; // Перенаправление на страницу логина, если партнер не авторизован
        }

        // Получаем отели, зарегистрированные партнером
        List<Hotel> hotels = hotelService.getHotelsByOwner(loggedInPartner);
        model.addAttribute("hotels", hotels);
        return "hotels_by_partner"; // Отображаем отели партнера
    }
    // Показ форми реєстрації готелю
//    @GetMapping("/hotel_registration")
//    public String showRegistrationForm(Model model) {
//        model.addAttribute("hotel", new Hotel());
//        model.addAttribute("partners", partnerService.getAllPartners());
//        return "hotel_form";
//    }


    @GetMapping("/edit/{id}")
    public String editHotel(@PathVariable Long id, Model model) {
        // Получаем отель по ID или выбрасываем исключение, если отель не найден
        Hotel hotel = hotelService.getHotelById(id)
                .orElseThrow(() -> new RuntimeException("Hotel not found"));
        model.addAttribute("hotel", hotel);

        // Получаем список всех партнеров для выбора владельца отеля
        List<Partner> partners = partnerService.getAllPartners();
        if (partners.isEmpty()) {
            throw new RuntimeException("No partners available");
        }
        model.addAttribute("partners", partners);

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

        // Валидация данных
        if (name.isEmpty() || address.isEmpty() || housingType.isEmpty()) {
            throw new IllegalArgumentException("Name, address, and housing type cannot be empty");
        }
        if (latitude != null && (latitude < -90 || latitude > 90)) {
            throw new IllegalArgumentException("Invalid latitude value");
        }
        if (longitude != null && (longitude < -180 || longitude > 180)) {
            throw new IllegalArgumentException("Invalid longitude value");
        }

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
        hotelService.saveHotelWithPartner(hotel, ownerPartner);

        // Перенаправляем на страницу списка отелей
        return "redirect:/hotels/hotel_list";
    }
    @PostMapping("/delete/{id}")
    public String deleteHotel(@PathVariable Long id) {
        // Проверяем, существует ли отель перед удалением
        Hotel hotel = hotelService.getHotelById(id)
                .orElseThrow(() -> new RuntimeException("Hotel not found"));

        // Удаляем отель
        hotelService.deleteHotel(id);

        // Перенаправляем на страницу списка отелей
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
//    @PostMapping("/hotels/register")
//    public String registerHotel(@ModelAttribute Hotel hotel, BindingResult result) {
//        if (result.hasErrors()) {
//            return "hotel_registration_form"; // верните страницу с формой, если есть ошибки
//        }
//        hotelService.saveHotel(hotel);
//        return "redirect:/hotels_by_partner"; // перенаправление после успешной регистрации
//    }
}
