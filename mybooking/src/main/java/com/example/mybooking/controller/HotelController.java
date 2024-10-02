package com.example.mybooking.controller;

import com.example.mybooking.model.*;
import com.example.mybooking.service.*;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    @Autowired
    public HotelController(HotelService hotelService, CityService cityService, PartnerService partnerService) {
        this.hotelService = hotelService;
        this.cityService = cityService;
        this.partnerService = partnerService;
    }


    // Метод для инициализации объекта отеля в сессии
    //проверяет, существует ли в сессии объект отеля. Если нет, создается новый объект отеля и сохраняется в сессии для дальнейшего использования.
    private Hotel initializeHotelInSession(HttpSession session) {
        Hotel sessionHotel = (Hotel) session.getAttribute("hotel");
        if (sessionHotel == null) {
            sessionHotel = new Hotel();
            session.setAttribute("hotel", sessionHotel);
            logger.info("New hotel object created and saved in session");
        }
        return sessionHotel;
    }
    // Получение всех отелей
    //получает список всех отелей с помощью сервиса HotelService и добавляет его в модель для отображения на странице hotel_list.html.
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
    // Показ формы для добавления отеля
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
                            @RequestParam(value = "images", required = false) List<MultipartFile> imageFiles) {
        // Получаем текущего авторизованного партнера
        Partner loggedInPartner = (Partner) session.getAttribute("loggedInPartner");
        if (loggedInPartner == null) {
            logger.error("Partner not found in session. Redirecting to login.");
            return "redirect:/partner_Account";
        }
        // Привязываем партнера к отелю
        hotel.setOwner(loggedInPartner);
        // Устанавливаем город по переданному cityId
        Optional<City> cityOptional = cityService.getCityById(cityId);
        if (cityOptional.isEmpty()) {
            logger.error("City with id {} not found", cityId);
            return "redirect:/hotels/add?error=city_not_found";
        }
        hotel.setCity(cityOptional.get());

        // Устанавливаем адрес
        hotel.setAddressStreet(addressStreet);

        // Привязываем удобства к отелю, если они были выбраны
        if (amenityIds != null && !amenityIds.isEmpty()) {
            Set<Amenity> amenities = amenityService.getAllAmenitiesByIds(amenityIds).stream().collect(Collectors.toSet());
            hotel.setAmenities(amenities); // Устанавливаем выбранные удобства
        }

        // Обрабатываем изображения, если они есть
        if (imageFiles != null && !imageFiles.isEmpty()) {
            try {
                Set<Image> images = processImages(imageFiles, hotel);
                hotel.setImages(images);
            } catch (IOException e) {
                logger.error("Error processing images: {}", e.getMessage());
                return "redirect:/hotels/add?error=image_processing_failed";
            }
        }

        // Сохраняем отель
        hotelService.saveHotelWithPartner(hotel, loggedInPartner, amenityIds );
        logger.info("Hotel successfully saved for partner: {}", loggedInPartner.getId());

        return "redirect:/hotels/hotels_by_partner";  // Перенаправляем на список отелей партнера
    }

    // Метод для обработки изображений
    private Set<Image> processImages(List<MultipartFile> imageFiles, Hotel hotel) throws IOException {
        Set<Image> imageSet = new HashSet<>();
        for (MultipartFile file : imageFiles) {
            if (!file.isEmpty()) {
                Image image = new Image();
                image.setUrl(file.getOriginalFilename());
                image.setPhotoBytes(file.getBytes());
                image.setHotel(hotel);
                imageSet.add(image);
                imageService.saveImage(image);
            }
        }
        return imageSet;
    }

    // Просмотр отелей, добавленных партнером
    @GetMapping("/hotels_by_partner")
    public String getHotelsByPartner(HttpSession session, Model model) {
        Partner loggedInPartner = (Partner) session.getAttribute("loggedInPartner");

        if (loggedInPartner == null) {
            return "redirect:/partner_Account";  // Перенаправляем на страницу логина
        }

        // Логируем идентификатор партнера
        logger.info("Logged in partner ID: {}", loggedInPartner.getId());

        List<Hotel> hotels = hotelService.getHotelsByOwner(loggedInPartner);

        // Логируем список отелей
        logger.info("Hotels retrieved: {}", hotels);

        model.addAttribute("hotels", hotels);
        return "hotels_by_partner";  // Отображаем отели, зарегистрированные партнером
    }


///////////////////////////////////////////////////////////////
// Обрабатываем обновление данных отеля в зависимости от шага
//        switch (step) {
//            case 1://Шаг 1: Проверяет наличие имени отеля и сохраняет его.
//                if (hotel.getName() == null || hotel.getName().isEmpty()) {
//                    logger.error("Hotel name is missing at step 1");
//                    return "redirect:/hotels/add?step=1&error=name";
//                }
//                if (housingType == null || housingType.isEmpty()) {
//                    logger.error("Housing type is missing at step 1");
//                    return "redirect:/hotels/add?step=1&error=housingType"; // Проверка типа жилья
//                }
//// Сохраняем имя отеля
//                sessionHotel.setName(hotel.getName());
//// Сохраняем тип жилья
//                sessionHotel.setHousingType(housingType);
//                break;
//            case 2://Шаг 2: Устанавливает город отеля с использованием CityService, а также адрес и координаты.
//                // Добавляем обработку города
////                Optional<City> cityOptional = cityService.getCityById(cityId);
////                if (cityOptional.isEmpty()) {
////                    logger.error("City not found at step 2");
////                    return "redirect:/hotels/add?step=2&error=city";
////                }
////                City city = cityOptional.get();
////// Устанавливаем город для отеля
////                hotel.setCity(city);
//                if (cityId == null) {
//                    logger.error("City ID is null");
//                    return "redirect:/hotels/add?step=2&error=city";
//                }
//                // Найдите город по ID
//                Optional<City> cityOptional = cityService.getCityById(cityId);
//                if (cityOptional.isEmpty()) {
//                    logger.error("City with id {} not found", cityId);
//                    return "redirect:/hotels/add?step=2&error=cityNotFound";
//                }
//
//                City city = cityOptional.get();
//                logger.info("City found: {}", city.getName());
//                sessionHotel.setCity(city);
//
//                // Устанавливаем адрес и координаты
//                if (addressStreet == null || addressStreet.isEmpty()) {
//                    logger.error("Address data is incomplete at step 2");
//                    return "redirect:/hotels/add?step=2&error=address";
//                }
//                sessionHotel.setAddressStreet(addressStreet);// Устанавливаем адрес
//                logger.info("Received address street: {}", addressStreet);
//
//                sessionHotel.setLatitude(hotel.getLatitude());
//                sessionHotel.setLongitude(hotel.getLongitude());
//                logger.info("Received coordinates: lat = {}, lon = {}", hotel.getLatitude(), hotel.getLongitude());
//                break;
//            case 3://Шаг 3: Устанавливает цену отеля и добавляет удобства (с помощью AmenityService).
//
//                sessionHotel.setPrice(hotel.getPrice());
//                if (sessionHotel.getPrice() == null || sessionHotel.getPrice() <= 0) {
//                    logger.error("Price is missing or invalid at step 3");
//                    return "redirect:/hotels/add?step=3&error=price";
//                }
//
//                List<Amenity> amenities = amenityService.getAllAmenities();
//                Set<Amenity> amenitySet = new HashSet<>(amenities);
//                sessionHotel.setAmenities(amenitySet);
//
//                if (sessionHotel.getPrice() == null || sessionHotel.getPrice() <= 0) {
//                    logger.info("Price is missing or invalid at step 3");
//                    return "redirect:/hotels/add?step=3&error=price";
//                }
//                break;
//
//            case 4://Шаг 4: Устанавливает описание отеля, изображения (с помощью ImageService), и номера (с помощью RoomService).
//                if (hotel.getDescription() == null || hotel.getDescription().isEmpty()) {
//                    logger.error("Description is missing at step 4");
//                    return "redirect:/hotels/add?step=4&error=description";
//                }
//
//                // Обновляем описание отеля в объекте sessionHotel
//                sessionHotel.setDescription(hotel.getDescription());
// // Обработка изображений
////Получение изображений: Идентификаторы изображений (ID) передаются с фронтенда и используются для получения соответствующих объектов Image из базы данных через ImageService.
////Установка изображений: Изображения добавляются в объект отеля, который хранится в сессии, и затем будет сохранен в базе данных на этапе завершения.
//                if (imageFiles != null && !imageFiles.isEmpty()) {
//                    Set<Image> imageSet = new HashSet<>();
//                    for (MultipartFile file : imageFiles) {
//                        if (!file.isEmpty()) {
//                            try {
//                                // Создаем объект Image
//                                Image image = new Image();
//                                image.setUrl(file.getOriginalFilename());
//                                image.setPhotoBytes(file.getBytes());
//                                image.setHotel(sessionHotel); // Привязываем изображение к отелю
//                                imageSet.add(image);
//                                imageService.saveImage(image); // Сохраняем изображение в базе данных
//                            } catch (IOException e) {
//                                logger.error("Error saving image: {}", e.getMessage());
//                            }
//                        }
//                    }
//                    sessionHotel.setImages(imageSet); // Связываем изображения с отелем
//                }
//                // Обработка номеров
////                List<Room> rooms = roomService.getRoomsByIds(roomIds);
////                Set<Room> roomSet = new HashSet<>(rooms);
////                sessionHotel.setRooms(roomSet);
//
//                hotelService.saveHotelWithPartner(sessionHotel, loggedInPartner);
//                break;
////            case 5://Шаг 5: Добавляет номер в отель, если введены данные о номере.
////                //добавлена возможность добавления номера в отель, если переданы его параметры (roomType, roomPrice, roomCapacity).
////
////                if (roomType != null && roomPrice != null && roomCapacity != null) {
////                    Room room = new Room(roomType, roomPrice, roomCapacity, sessionHotel);
////                    roomService.saveRoom(room); // Сохраняем номер в базе данных
////                    logger.info("Room successfully added: {}", room);
////                }
////                break;
//            default:
//                logger.error("Invalid step: {}", step);
//                return "redirect:/hotels/add";
//        }
//
//        // Сохраняем объект отеля в сессии после каждого шага
//        session.setAttribute("hotel", sessionHotel);
//        logger.info("Hotel object successfully updated and saved in session after step {}", step);
//
//        // Переход на следующий шаг
//        return "redirect:/hotels/add?step=" + (step + 1);
//    }
    @PostMapping("/submit")
    public String submitHotel(HttpSession session) {
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
            hotelService.saveHotelWithPartner(sessionHotel, loggedInPartner, amenityIds);
            logger.info("Hotel successfully saved in the database with partner: {}", loggedInPartner.getId());

            // Удаляем объект отеля из сессии после успешного сохранения
            session.removeAttribute("hotel");// Очищаем объект отеля из сессии
            session.removeAttribute("amenityIds");  // Удаляем удобства из сессии
            logger.info("Hotel object removed from session after successful save.");
        } catch (Exception e) {
            logger.error("Error while saving hotel: {}", e.getMessage(), e);
            return "redirect:/hotels/add?error=save_failed";
        }

        return "redirect:/hotels_by_partner";
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
    @PostMapping("/add_hotel")
    public String addHotel(@ModelAttribute("hotel") Hotel hotel, BindingResult result, Model model,
                           @RequestParam(value = "amenities", required = false) List<Long> amenityIds) {
        if (result.hasErrors()) {
            // Повертаємо всі дані для форми в разі помилки
            model.addAttribute("hotels", hotelService.getAllHotels());
            model.addAttribute("cities", cityService.getAllCities());
            model.addAttribute("partners", partnerService.getAllPartners());
            return "/hotels/hotel_list";
        }

        // Отримуємо партнера за ID
        Partner partner = partnerService.getPartnerById(hotel.getOwner().getId())
                .orElseThrow(() -> new IllegalArgumentException("Невірний ID партнера: " + hotel.getOwner().getId()));

        // Зберігаємо готель з партнером
        hotelService.saveHotelWithPartner(hotel, partner, amenityIds);
        return "redirect:/hotels/hotel_list";
    }

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

//    public void deleteHotel(Long id) {
//        logger.debug("Deleting hotel with ID: {}", id);
//        if (hotelRepository.existsById(id)) {
//            hotelRepository.deleteById(id);
//            logger.info("Hotel with ID: {} successfully deleted", id);
//        } else {
//            logger.warn("Hotel with ID: {} not found", id);
//        }
//    }

@PostMapping("/delete/{id}")
public String deleteHotel(@PathVariable("id") Long id, HttpSession session) {
    // Получаем текущего авторизованного партнера
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

    //Опис готеля - перехід на окрему сторінку конкретного готеля
    @GetMapping("/{id}")
    public String getHotelDetails(@PathVariable("id") Long id, Model model) {
        Optional<Hotel> hotelOptional = hotelService.getHotelById(id);

        if (hotelOptional.isPresent()) {
            model.addAttribute("hotel", hotelOptional.get());
            return "hotel_details";
        } else {
            return "hotel_not_found"; // Перенаправляємо на сторінку з помилкою або обробляємо іншим способом
        }
    }

}
