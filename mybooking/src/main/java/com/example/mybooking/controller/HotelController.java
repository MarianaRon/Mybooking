package com.example.mybooking.controller;

import com.example.mybooking.model.*;
import com.example.mybooking.service.*;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

//////////////////////////////////////////////23
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


    @Autowired
    private CityService cityService; // Добавляем CityService для работы с городами
//    @Autowired
//    public HotelController(HotelService hotelService, CityService cityService, PartnerService partnerService) {
//        this.hotelService = hotelService;
//        this.cityService = cityService;
//        this.partnerService = partnerService;
//    }


    // Метод для инициализации объекта отеля в сессии
    //проверяет, существует ли в сессии объект отеля. Если нет, создается новый объект отеля и сохраняется в сессии для дальнейшего использования.
//    private Hotel initializeHotelInSession(HttpSession session) {
//        Hotel sessionHotel = (Hotel) session.getAttribute("hotel");
//        if (sessionHotel == null) {
//            sessionHotel = new Hotel();
//            session.setAttribute("hotel", sessionHotel);
//            logger.info("New hotel object created and saved in session");
//        }
//        return sessionHotel;
//    }
    // Получение всех отелей
    //получает список всех отелей с помощью сервиса HotelService и добавляет его в модель для отображения на странице hotel_list.html.

    // Получение всех отелей
    @GetMapping
    public String getAllHotels(Model model) {
        List<Hotel> hotels = hotelService.getAllHotels();
        model.addAttribute("hotels", hotels);
        return "hotels/hotel_list";
    }
    // Получение отеля по ID
    //получает отель по его ID через сервис HotelService. Если отель найден, он отображается на странице hotel_details.html. Если нет, происходит перенаправление на список отелей.
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

    //метод отображает форму для добавления отеля. Также осуществляется проверка авторизованного партнера, так как только партнеры могут добавлять отели.
    @GetMapping("/add")
    public String showAddHotelForm(HttpSession session, Model model) {
        // Создаем новый объект отеля
        model.addAttribute("hotel", new Hotel());


        // Получаем список удобств
        List<Amenity> amenities = amenityService.getAllAmenities();

        if (amenities == null || amenities.isEmpty()) {
            logger.warn("Список удобств пуст или не был загружен.");
        } else {
            logger.info("Список удобств загружен: {}", amenities);
        }
        model.addAttribute("amenities", amenities);

// Загружаем список городов
        List<City> cities = cityService.getAllCities();
        model.addAttribute("cities", cities);

        // Проверяем, авторизован ли партнер
        Partner loggedInPartner = (Partner) session.getAttribute("loggedInPartner");
        if (loggedInPartner == null) {
            return "redirect:/partner_Account";  // Перенаправляем на логин, если не авторизован
        }

        return "add_hotels";  // Возвращаем форму для регистрации отеля
    }
    // Обработка и сохранение данных отеля
    @PostMapping("/add")
    public String saveHotel(@ModelAttribute Hotel hotel, HttpSession session,
                            @RequestParam("cityId") Long cityId,
                            @RequestParam("addressStreet") String addressStreet,
                            @RequestParam(value = "amenities",required = false) List<Long> amenityIds,
                            @RequestParam(value = "latitude", required = false) String latitudeStr,
                            @RequestParam(value = "longitude", required = false) String longitudeStr,
                            @RequestParam(value = "coverImage", required = false) MultipartFile coverImageFile,
                            @RequestParam(value = "imageFiles", required = false) Set<MultipartFile> imageFiles) {



        // Получаем текущего авторизованного партнера
        Partner loggedInPartner = (Partner) session.getAttribute("loggedInPartner");
        if (loggedInPartner == null) {
            logger.error("Partner not found in session. Redirecting to login.");
            return "redirect:/partner_Account";
        }
        // Привязываем партнера к отелю
        hotel.setOwner(loggedInPartner);


        // Логируем входные данные
        logger.info("Received hotel data: {}", hotel);
        logger.info("Received cityId: {}", cityId);
        logger.info("Received addressStreet: {}", addressStreet);
        logger.info("Received latitude: {}", latitudeStr);
        logger.info("Received longitude: {}", longitudeStr);
        logger.info("Полученные идентификаторы удобств: {}", amenityIds);

        if (coverImageFile != null && !coverImageFile.isEmpty()) {
            try {
                hotel.setCoverImage(coverImageFile.getBytes());
            } catch (IOException e) {
                logger.error("Error processing cover image file: {}", e.getMessage());
            }
        }

        if (imageFiles != null && !imageFiles.isEmpty()) {
            logger.info("Received {} additional image(s).", imageFiles.size());
            for (MultipartFile file : imageFiles) {
                logger.info("Received additional image: {} (size: {} bytes)", file.getOriginalFilename(), file.getSize());
            }
        } else {
            logger.warn("No additional images received.");
        }

        // Устанавливаем город по переданному cityId
        Optional<City> cityOptional = cityService.getCityById(cityId);
        if (cityOptional.isEmpty()) {
            logger.error("City with id {} not found", cityId);
            return "redirect:/hotels/add?error=city_not_found";
        }
        hotel.setCity(cityOptional.get());

        // Устанавливаем адрес
        hotel.setAddressStreet(addressStreet);

        // Преобразование latitude и longitude в double
        try {
            if (latitudeStr != null && !latitudeStr.isEmpty()) {
                hotel.setLatitude(Double.parseDouble(latitudeStr));
            }
            if (longitudeStr != null && !longitudeStr.isEmpty()) {
                hotel.setLongitude(Double.parseDouble(longitudeStr));
            }
        } catch (NumberFormatException e) {
            logger.error("Invalid latitude or longitude format: {}", e.getMessage());
            return "redirect:/hotels/add?error=invalid_coordinates";
        }


        // Сохраняем отель с обложкой и изображениями
        hotelService.saveHotelWithPartner(hotel, loggedInPartner, amenityIds, coverImageFile, imageFiles);
        return "redirect:/hotels/hotels_by_partner";
    }


    // Просмотр отелей, добавленных партнером
    @GetMapping("/hotels_by_partner")
    public String getHotelsByPartner(HttpSession session, Model model) {
        Partner loggedInPartner = (Partner) session.getAttribute("loggedInPartner");

        if (loggedInPartner == null) {
            return "redirect:/home_partners";  // Перенаправляем на страницу логина
        }

        // Логируем идентификатор партнера
        logger.info("Logged in partner ID: {}", loggedInPartner.getId());

        List<Hotel> hotels = hotelService.getHotelsByOwner(loggedInPartner);

        // Логируем список отелей
        logger.info("Hotels retrieved: {}", hotels);

        model.addAttribute("hotels", hotels);
        return "redirect:/hotels/hotels_by_partner";  // Отображаем отели, зарегистрированные партнером
    }


    @PostMapping("/submit")
    public String submitHotel(HttpSession session,
                              @RequestParam(value = "coverImage", required = false) MultipartFile coverImageFile,
                              @RequestParam(value = "imageFiles", required = false) Set<MultipartFile> imageFiles) {
        // Проверяем наличие объекта отеля в сессии
        Hotel sessionHotel = (Hotel) session.getAttribute("hotel");

        if (sessionHotel == null) {
            logger.error("Hotel not found in session during submit. Redirecting back to form.");
            return "redirect:/hotels/add?error=hotel_not_found";
        }

        // Проверяем наличие авторизованного партнера
        Partner loggedInPartner = (Partner) session.getAttribute("loggedInPartner");
        if (loggedInPartner == null) {
            logger.error("Partner not found in session during submit. Redirecting to login.");
            return "redirect:/partner_Account";
        }

        // Получаем список выбранных удобств из сессии
        @SuppressWarnings("unchecked")
        List<Long> amenityIds = (List<Long>) session.getAttribute("amenityIds");

        // Логируем данные перед сохранением
        logger.info("Submitting hotel for partner {}: {}", loggedInPartner.getId(), sessionHotel);

        try {
            // Сохраняем отель в базе данных
            hotelService.saveHotelWithPartner(sessionHotel, loggedInPartner, amenityIds, coverImageFile, imageFiles);
            logger.info("Hotel successfully saved in the database with partner: {}", loggedInPartner.getId());

            // Удаляем объект отеля из сессии после успешного сохранения
            session.removeAttribute("hotel"); // Очищаем объект отеля из сессии
            session.removeAttribute("amenityIds");  // Удаляем удобства из сессии
            logger.info("Hotel object removed from session after successful save.");
        } catch (Exception e) {
            logger.error("Error while saving hotel: {}", e.getMessage(), e);
            return "redirect:/hotels/add?error=save_failed";
        }
        return "redirect:/hotels/hotels_by_partner";
        //return "redirect:/hotels_by_partner";
    }


    // Виведення списку готелів і форма для додавання
    @GetMapping("/hotel_list")
    public String showHotelList(Model model) {
        List<Hotel> hotels = hotelService.getAllHotels();
        model.addAttribute("hotels", hotels);
        model.addAttribute("hotel", new Hotel()); // Для форми додавання
        model.addAttribute("cities", cityService.getAllCities());
        model.addAttribute("partners", partnerService.getAllPartners());
        return "/hotels/hotel_list"; // Повертаємо шаблон зі списком та формою
    }

    // Додавання нового готелю через форму на тій же сторінці
//    @PostMapping("/add_hotel")
//    public String addHotel(@ModelAttribute("hotel") Hotel hotel, BindingResult result, Model model,
//                           @RequestParam(value = "amenities", required = false) List<Long> amenityIds) {
//        if (result.hasErrors()) {
//            // Повертаємо всі дані для форми в разі помилки
//            model.addAttribute("hotels", hotelService.getAllHotels());
//            model.addAttribute("cities", cityService.getAllCities());
//            model.addAttribute("partners", partnerService.getAllPartners());
//            return "/hotels/hotel_list";
//        }
//
//        // Отримуємо партнера за ID
//        Partner partner = partnerService.getPartnerById(hotel.getOwner().getId())
//                .orElseThrow(() -> new IllegalArgumentException("Невірний ID партнера: " + hotel.getOwner().getId()));
//
//        // Зберігаємо готель з партнером
//        hotelService.saveHotelWithPartner(hotel, partner, amenityIds, coverImageFile, imageFiles);
//        return "redirect:/hotels/hotel_list";
//    }

    // Показати форму для редагування готелю
    @GetMapping("/edit/{id}")
    public String showEditHotelForm(@PathVariable("id") Long id, Model model) {
        Hotel hotel = hotelService.getHotelById(id)
                .orElseThrow(() -> new IllegalArgumentException("Невірний ID готелю: " + id));
        model.addAttribute("hotel", hotel);
        model.addAttribute("cities", cityService.getAllCities());
        model.addAttribute("partners", partnerService.getAllPartners());
        return "/hotels/edit_hotel"; // Thymeleaf шаблон для редагування
    }

    // Оновлення готелю через форму редагування
    @PostMapping("/edit/{id}")
    public String editHotel(@PathVariable("id") Long id, @ModelAttribute("hotel") Hotel hotel, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("cities", cityService.getAllCities());
            model.addAttribute("partners", partnerService.getAllPartners());
            return "/hotels/edit_hotel";
        }

        hotelService.updateHotel(id, hotel);
        return "redirect:/hotels/hotel_list";
    }

    //@PostMapping("/delete/{id}")
//public String deleteHotel(@PathVariable("id") Long id, HttpSession session) {
//    // Получаем текущего авторизованного партнера
//    Partner loggedInPartner = (Partner) session.getAttribute("loggedInPartner");
//    if (loggedInPartner == null) {
//        return "redirect:/partner_Account"; // Перенаправляем на логин, если не авторизован
//    }
//
//    // Удаляем отель
//    boolean deleted = hotelService.deleteHotelById(id);
//
//    if (deleted) {
//        // Логируем успешное удаление
//        logger.info("Hotel with ID: {} successfully deleted by partner ID: {}", id, loggedInPartner.getId());
//    } else {
//        logger.warn("Hotel with ID: {} not found for deletion", id);
//    }
//
//    // Перенаправляем на страницу отелей партнера
//    return "redirect:/hotels/hotels_by_partner";
//}
// Удаление отеля
    @PostMapping("/delete/{id}")
    public String deleteHotel(@PathVariable("id") Long id, HttpSession session) {
        Partner loggedInPartner = (Partner) session.getAttribute("loggedInPartner");
        if (loggedInPartner == null) {
            return "redirect:/partner_Account";
        }

        boolean deleted = hotelService.deleteHotelById(id);
        if (deleted) {
            logger.info("Hotel with ID: {} successfully deleted by partner ID: {}", id, loggedInPartner.getId());
        } else {
            logger.warn("Hotel with ID: {} not found for deletion", id);
        }

        return "redirect:/hotels/hotels_by_partner";
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

    //Опис готеля - перехід на окрему сторінку конкретного готеля
    @GetMapping("/{id}")
    public String getHotelDetails(@PathVariable("id") Long id, Model model) {
        Optional<Hotel> hotelOptional = hotelService.getHotelById(id);

        if (hotelOptional.isPresent()) {
            Hotel hotel = hotelOptional.get();
            model.addAttribute("hotel", hotelOptional.get());
            model.addAttribute("city", hotelOptional.get().getCity());
            model.addAttribute("rooms", hotelOptional.get().getRooms());
            model.addAttribute("amenities", hotel.getAmenities());
            return "hotel_details";
        } else {
            return "hotel_not_found"; // Перенаправляємо на сторінку з помилкою або обробляємо іншим способом
        }
    }

}