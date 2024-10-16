package com.example.mybooking.controller;

import com.example.mybooking.model.*;
import com.example.mybooking.repository.IAmenityRepository;
import com.example.mybooking.repository.IImageRepository;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
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
    private ImageService imageService;
    @Autowired
    private IImageRepository imageRepository;


    @Autowired
    private RoomService roomService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private AmenityService amenityService;

    @Autowired
    private IAmenityRepository amenityRepository;
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

    //Обработчик для отображения формы добавления отеля
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
            return "redirect:/home_partners";  // Перенаправляем на логин, если не авторизован
        }

        return "add_hotels";  // Возвращаем форму для регистрации отеля
    }

    @GetMapping("/hotels/{id}/cover")
    @ResponseBody
    public ResponseEntity<byte[]> getHotelCover(@PathVariable Long id) {
        Hotel hotel = hotelService.getHotelById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid hotel Id: " + id));
        byte[] coverImage = hotel.getCoverImage();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.IMAGE_JPEG);

        return new ResponseEntity<>(coverImage, headers, HttpStatus.OK);
    }

    @PostMapping("/add")
    public String saveHotel(HttpSession session,
                            @RequestParam("name") String name, // Добавьте поле name
                            @RequestParam("cityId") Long cityId,
                            @RequestParam("addressStreet") String addressStreet,
                            @RequestParam("price") Double price,
                            @RequestParam("description") String description,
                            @RequestParam("housingType") String housingType,//housingType
                            @RequestParam(value = "amenities",required = false) List<Long> amenityIds,
                            @RequestParam(value = "latitude", required = false) String latitudeStr,
                            @RequestParam(value = "longitude", required = false) String longitudeStr,
                            @RequestParam("coverImage") MultipartFile coverImageFile, // Вместо привязки к модели
                            @RequestParam(value = "imageFiles", required = false) List<MultipartFile> imageFiles) {
        Hotel hotel = new Hotel();
        // Устанавливаем имя отеля
        hotel.setName(name);
        // Устанавливаем цену отеля
        hotel.setPrice(price);
        // Устанавливаем описание отеля
        hotel.setDescription(description);
        // Устанавливаем тип жилья
        hotel.setHousingType(housingType);

        // Получаем текущего авторизованного партнера
        Partner loggedInPartner = (Partner) session.getAttribute("loggedInPartner");
        if (loggedInPartner == null) {
            logger.error("Partner not found in session. Redirecting to login.");
            return "redirect:/partner_Account";
        }
        // Устанавливаем партнера как владельца отеля
        hotel.setOwner(loggedInPartner);

        // Проверяем, существует ли город с указанным ID
        Optional<City> cityOptional = cityService.getCityById(cityId);
        if (cityOptional.isEmpty()) {
            logger.error("City with id {} not found", cityId);
            return "redirect:/hotels/add?error=city_not_found";
        }

        // Устанавливаем город для отеля
        hotel.setCity(cityOptional.get());

        // Устанавливаем адрес для отеля
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

        // Привязываем удобства
        if (amenityIds != null && !amenityIds.isEmpty()) {
            Set<Amenity> amenities = new HashSet<>(amenityService.getAllAmenitiesByIds(amenityIds));
            hotel.setAmenities(amenities);
        }

        // Обработка файла обложки отеля
        try {
            if (!coverImageFile.isEmpty()) {
                hotel.setCoverImage(coverImageFile.getBytes()); // Извлечение байтов из MultipartFile
            } else {
                logger.warn("Cover image is missing for hotel: {}", hotel.getName());
                throw new IllegalArgumentException("Cover image is required"); // Вызываем исключение, если файл пуст
            }
        } catch (IOException e) {
            logger.error("Error uploading cover image: {}", e.getMessage());
            return "redirect:/hotels/add?error=cover_image_upload_failed"; // Обработка ошибки
        }

        // Обработка дополнительных изображений
        Set<Image> images = new HashSet<>();
        if (imageFiles != null) {
            for (MultipartFile imageFile : imageFiles) {
                if (!imageFile.isEmpty()) {
                    try {
                        Image image = new Image();
                        image.setPhotoBytes(imageFile.getBytes());

                        // Устанавливаем URL для изображения
                        String imageUrl = "/images/" + imageFile.getOriginalFilename(); // Замените это на вашу логику
                        image.setUrl(imageUrl); // Устанавливаем URL

                        image.setHotel(hotel);
                        images.add(image);
                    } catch (IOException e) {
                        logger.error("Error processing image file: {}", e.getMessage());
                        return "redirect:/hotels/add?error=image_upload_failed";
                    }
                }
            }
        }
        hotel.setImages(images); // Устанавливаем изображения в отель

        // Сохранение отеля
        hotelService.save(hotel);
        return "redirect:/hotels/hotels_by_partner";
    }

    // Просмотр отелей, добавленных партнером
    @GetMapping("/hotels_by_partner")
    public String getHotelsByPartner(HttpSession session, Model model) {
        Partner loggedInPartner = (Partner) session.getAttribute("loggedInPartner");

        if (loggedInPartner == null) {
            return "redirect:/exit_Account";  // Перенаправляем на страницу логина
        }

        // Логируем идентификатор партнера
        logger.info("Logged in partner ID: {}", loggedInPartner.getId());

        List<Hotel> hotels = hotelService.getHotelsByOwner(loggedInPartner);

        // Логируем список отелей
        logger.info("Hotels retrieved: {}", hotels);

        model.addAttribute("hotels", hotels);
        return "hotels_by_partner";  // Отображаем отели, зарегистрированные партнером
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

// Удаление отеля
    @PostMapping("/hotels_delete/{id}")
    public String deleteHotel(@PathVariable("id") Long id, HttpSession session) {
        logger.info("Attempting to delete hotel with ID: {}", id);
        Partner loggedInPartner = (Partner) session.getAttribute("loggedInPartner");
        if (loggedInPartner == null) {
            return "redirect:/partner_Account"; // Перенаправляем на логин, если не авторизован
        }
        // Удаляем отель
        boolean deleted = hotelService.deleteHotelById(id);

        if (deleted) {
            // Логируем успешное удаление
            logger.info("Hotel with ID: {} successfully deleted by partner ID: {}", id, loggedInPartner.getId());
        } else {
            logger.warn("Hotel with ID: {} not found for deletion", id);
        }

        // Перенаправляем на страницу отелей партнера
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

    @PostMapping("/add_hotel")
    public String addHotel(@ModelAttribute Hotel hotel,
                           @RequestParam("coverImage") MultipartFile coverImageFile,
                           @RequestParam("city") Long cityId,
                           @RequestParam("owner") Long partnerId,
                           RedirectAttributes redirectAttributes) throws IOException {

        // Отримуємо місто та власника за їх ідентифікаторами
        Optional<City> cityOptional = cityService.getCityById(cityId);
        Optional<Partner> partnerOptional = partnerService.getPartnerById(partnerId);

        // Перевіряємо, чи знайдені місто і власник
        if (cityOptional.isPresent() && partnerOptional.isPresent()) {
            hotel.setCity(cityOptional.get());
            hotel.setOwner(partnerOptional.get());

            // Зберігаємо зображення як байтовий масив
            if (!coverImageFile.isEmpty()) {
                byte[] coverImageBytes = coverImageFile.getBytes();
                hotel.setCoverImage(coverImageBytes);
            }
            // Зберігаємо готель
            hotelService.save(hotel);
            return "redirect:/hotels/hotel_list";
        } else {
            // Якщо місто або власник не знайдені, відображаємо помилку
            redirectAttributes.addFlashAttribute("error", "Місто або власник не знайдені");
            return "redirect:/hotels/hotel_list";
        }
    }

    @GetMapping("/hotels/coverImage/{id}")
    @ResponseBody
    public ResponseEntity<byte[]> getHotelCoverImage(@PathVariable("id") Long hotelId) {
        Optional<Hotel> hotelOptional = hotelService.getHotelById(hotelId);
        if (hotelOptional.isPresent() && hotelOptional.get().getCoverImage() != null) {
            byte[] image = hotelOptional.get().getCoverImage();
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG) // або IMAGE_PNG залежно від типу зображення
                    .body(image);
        } else {
            return ResponseEntity.notFound().build();
        }
    }



    @GetMapping("/{id}")
    public String getHotelDetails(@PathVariable("id") Long id, Model model) {
        Optional<Hotel> hotelOptional = hotelService.getHotelById(id);  // Отримання готелю за ID

        if (hotelOptional.isPresent()) {
            Hotel hotel = hotelOptional.get();
            Set<Review> reviews = hotel.getReviews();  // Отримання списку відгуків

            // Обчислення середнього рейтингу
            double averageRating = 0.0;
            if (!reviews.isEmpty()) {
                averageRating = reviews.stream()
                        .mapToDouble(Review::getRating)
                        .average()
                        .orElse(0.0);  // Обчислення середнього рейтингу
                averageRating = Math.round(averageRating * 10.0) / 10.0;
            }
            // Додаємо зображення (обкладинку) готелю, якщо воно є
            List<Image> images = imageService.getImagesByHotelId(id);
            if (!images.isEmpty()) {
                hotel.setCoverImage(images.get(0).getPhotoBytes());  // Використовуємо перше зображення як обкладинку
            }

            // Додавання атрибутів до моделі
            model.addAttribute("hotel", hotel);
            model.addAttribute("city", hotel.getCity());
            model.addAttribute("rooms", hotel.getRooms());
            model.addAttribute("amenities", hotel.getAmenities());
            model.addAttribute("averageRating", averageRating);

            return "hotel_details";  // Повернення шаблону сторінки з деталями готелю
        } else {
            return "hotel_not_found";  // Сторінка помилки, якщо готель не знайдено
        }

    }
}