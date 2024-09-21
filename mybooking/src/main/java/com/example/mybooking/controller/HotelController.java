package com.example.mybooking.controller;

import com.example.mybooking.model.*;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartFile;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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

    //Многошаговая форма: добавление отеля Методы для добавления отеля разбиты на шаги, где информация сохраняется поэтапно, что позволяет разделить ввод данных на несколько шагов

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

    // Показ формы для добавления отеля
    //метод отображает форму для добавления отеля. Также осуществляется проверка авторизованного партнера, так как только партнеры могут добавлять отели.
    @GetMapping("/add")
    public String showAddHotelForm(HttpSession session, Model model, HttpServletResponse response) {
        Hotel hotel = new Hotel();
        model.addAttribute("hotel", hotel);


        // Получаем список удобств из базы данных
        List<Amenity> amenities = amenityService.getAllAmenities();
        logger.info("Loaded amenities: {}", amenities); // Логирование для проверки
        model.addAttribute("amenities", amenities); // Передаем удобства в модель
        ;

        // Проверяем, передаются ли данные об удобствах
        System.out.println("Удобства загружены: " + amenities);

        // Отключаем кэширование для страницы добавления отеля
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1
        response.setHeader("Pragma", "no-cache"); // HTTP 1.0
        response.setDateHeader("Expires", 0); // Прокси

        // Получаем текущего авторизованного партнера
        Partner loggedInPartner = (Partner) session.getAttribute("loggedInPartner");
        if (loggedInPartner == null) {
            // Переход на страницу авторизации, если партнер не авторизован
            return "redirect:/partner_Account";
        }
// Возвращаем шаблон страницы добавления отеля
        return "add_hotels";
    }
    @PostMapping("/add")
    public String processFormStep(@ModelAttribute Hotel hotel, HttpSession session,
                                  @RequestParam("cityId") Long cityId, // ID города
                                  @RequestParam("amenities") List<Long> amenityIds,
                                  @RequestParam("images") List<MultipartFile> imageFiles, // Загрузка изображений
                                  @RequestParam("rooms") List<Long> roomIds,
                                  @RequestParam("step") int step,
                                  @RequestParam(required = false) String roomType,
                                  @RequestParam(required = false) Double roomPrice,
                                  @RequestParam(required = false) Integer roomCapacity,
                                  //Добавляем параметр housingType)
                                  @RequestParam(required = false) String housingType) {
// Инициализация объекта отеля в сессии
        Hotel sessionHotel = initializeHotelInSession(session);

        logger.info("Processing step {}. Hotel in session: {}", step, sessionHotel);

// Проверка, есть ли в сессии авторизованный партнер
        Partner loggedInPartner = (Partner) session.getAttribute("loggedInPartner");
        if (loggedInPartner == null) {
            logger.error("Partner not found in session. Redirecting to login.");
// Если партнер не авторизован
            return "redirect:/partner_Account";
        }
// Привязываем партнера к отелю
        sessionHotel.setOwner(loggedInPartner);

// Обрабатываем обновление данных отеля в зависимости от шага
        switch (step) {
            case 1://Шаг 1: Проверяет наличие имени отеля и сохраняет его.
                if (hotel.getName() == null || hotel.getName().isEmpty()) {
                    logger.error("Hotel name is missing at step 1");
                    return "redirect:/hotels/add?step=1&error=name";
                }
                if (housingType == null || housingType.isEmpty()) {
                    logger.error("Housing type is missing at step 1");
                    return "redirect:/hotels/add?step=1&error=housingType"; // Проверка типа жилья
                }
// Сохраняем имя отеля
                sessionHotel.setName(hotel.getName());
// Сохраняем тип жилья
                sessionHotel.setHousingType(housingType);
                break;
            case 2://Шаг 2: Устанавливает город отеля с использованием CityService, а также адрес и координаты.
                // Добавляем обработку города
                Optional<City> cityOptional = cityService.getCityById(cityId);
                if (cityOptional.isEmpty()) {
                    logger.error("City not found at step 2");
                    return "redirect:/hotels/add?step=2&error=city";
                }
                City city = cityOptional.get();
// Устанавливаем город для отеля
                hotel.setCity(city);

                if (hotel.getAddressStreet() == null) {
                    logger.error("Address data is incomplete at step 2");
                    return "redirect:/hotels/add?step=2&error=address";
                }
                sessionHotel.setAddressStreet(hotel.getAddressStreet());
                sessionHotel.setLatitude(hotel.getLatitude());
                sessionHotel.setLongitude(hotel.getLongitude());
                break;
            case 3://Шаг 3: Устанавливает цену отеля и добавляет удобства (с помощью AmenityService).

                sessionHotel.setPrice(hotel.getPrice());

                List<Amenity> amenities = amenityService.getAmenitiesByIds(amenityIds);
                Set<Amenity> amenitySet = new HashSet<>(amenities);
                sessionHotel.setAmenities(amenitySet);

                if (hotel.getPrice() == null || hotel.getPrice() <= 0) {
                    logger.error("Price is missing or invalid at step 3");
                    return "redirect:/hotels/add?step=3&error=price";
                }
                break;

            case 4://Шаг 4: Устанавливает описание отеля, изображения (с помощью ImageService), и номера (с помощью RoomService).
                if (hotel.getDescription() == null || hotel.getDescription().isEmpty()) {
                    logger.error("Description is missing at step 4");
                    return "redirect:/hotels/add?step=4&error=description";
                }

                sessionHotel.setDescription(hotel.getDescription());
 // Обработка изображений
//Получение изображений: Идентификаторы изображений (ID) передаются с фронтенда и используются для получения соответствующих объектов Image из базы данных через ImageService.
//Установка изображений: Изображения добавляются в объект отеля, который хранится в сессии, и затем будет сохранен в базе данных на этапе завершения.
                // Обработка и сохранение изображений
                if (imageFiles != null && !imageFiles.isEmpty()) {
                    Set<Image> imageSet = new HashSet<>();
                    for (MultipartFile file : imageFiles) {
                        try {
                            Image image = new Image();
                            image.setUrl(file.getOriginalFilename()); // URL или имя файла
                            image.setPhotoBytes(file.getBytes()); // Байтовое содержимое изображения
                            image.setHotel(sessionHotel);
                            imageSet.add(image);
                            imageService.saveImage(image); // Сохранение изображения в базе данных
                        } catch (Exception e) {
                            logger.error("Error saving image: {}", e.getMessage());
                        }
                    }
                    sessionHotel.setImages(imageSet);
                }

                // Обработка номеров
                List<Room> rooms = roomService.getRoomsByIds(roomIds);
                Set<Room> roomSet = new HashSet<>(rooms);
                sessionHotel.setRooms(roomSet);
                break;
            case 5://Шаг 5: Добавляет номер в отель, если введены данные о номере.
                //добавлена возможность добавления номера в отель, если переданы его параметры (roomType, roomPrice, roomCapacity).

                if (roomType != null && roomPrice != null && roomCapacity != null) {
                    Room room = new Room(roomType, roomPrice, roomCapacity, sessionHotel);
                    roomService.saveRoom(room); // Сохраняем номер в базе данных
                    logger.info("Room successfully added: {}", room);
                }
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
            session.removeAttribute("hotel");// Очищаем объект отеля из сессии
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

        // Обновление данных отеля
        hotel.setName(name);
        hotel.setDescription(description);
       // hotel.setAddress(address);
        hotel.setLatitude(latitude);
        hotel.setLongitude(longitude);
        hotel.setOwner(ownerPartner); // Устанавливаем владельца
        //hotel.setHousingType(housingType);

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
