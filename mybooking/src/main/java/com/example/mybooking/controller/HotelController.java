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
    /////////////////////////////
    @GetMapping("/edit_full/{id}")
    public String showFullEditHotelForm(@PathVariable("id") Long id, HttpSession session, Model model) {
        // Проверяем, авторизован ли партнер
        Partner loggedInPartner = (Partner) session.getAttribute("loggedInPartner");
        if (loggedInPartner == null) {
            return "redirect:/exit_Account";  // Перенаправляем на страницу логина
        }

        // Получаем отель по ID
        Hotel hotel = hotelService.getHotelById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid hotel ID: " + id));

        // Проверяем, что партнёр является владельцем отеля
        if (!hotel.getOwner().getId().equals(loggedInPartner.getId())) {
            return "redirect:/hotels/hotels_by_partner";  // Перенаправляем, если партнёр не владелец
        }

        // Добавляем данные отеля в модель для отображения
        model.addAttribute("hotel", hotel);  // Основные данные отеля (name, description, и т.д.)

        // Добавляем список всех городов для выпадающего списка
        model.addAttribute("cities", cityService.getAllCities());

        // Добавляем список всех удобств для редактирования
        model.addAttribute("amenities", amenityService.getAllAmenities());

        // Добавляем текущие изображения отеля (если есть)
        model.addAttribute("images", hotel.getImages());

        return "edit_full_hotel";  // Thymeleaf шаблон для редактирования полной информации
    }
    // Обработчик для сохранения изменений полной информации об отеле
    @PostMapping("/edit_full/{id}")
    public String saveFullHotelEdit(@PathVariable("id") Long id,
                                    @RequestParam("name") String name,
                                    @RequestParam("cityId") Long cityId,
                                    @RequestParam("addressStreet") String addressStreet,
                                    @RequestParam("price") Double price,
                                    @RequestParam("description") String description,
                                    @RequestParam(value = "amenities", required = false) List<Long> amenityIds,
                                    @RequestParam(value = "coverImage", required = false) MultipartFile coverImageFile,
                                    @RequestParam(value = "imageFiles", required = false) List<MultipartFile> imageFiles,
                                    @RequestParam(value = "deleteImages", required = false) List<Long> deleteImageIds,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {


        // Проверяем, авторизован ли партнёр
        Partner loggedInPartner = (Partner) session.getAttribute("loggedInPartner");
        if (loggedInPartner == null) {
            return "redirect:/exit_Account";
        }

        // Получаем отель по ID
        Hotel hotel = hotelService.getHotelById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid hotel ID: " + id));

        // Проверяем, что партнёр является владельцем отеля
        if (!hotel.getOwner().getId().equals(loggedInPartner.getId())) {
            return "redirect:/hotels/hotels_by_partner";
        }

        // Обновляем данные отеля
        hotel.setName(name);
        hotel.setDescription(description);
        hotel.setPrice(price);
        hotel.setCity(cityService.getCityById(cityId).orElseThrow(() -> new IllegalArgumentException("Invalid city ID")));
        hotel.setAddressStreet(addressStreet);

        // Привязываем удобства
        if (amenityIds != null) {
            Set<Amenity> amenities = new HashSet<>(amenityService.getAllAmenitiesByIds(amenityIds));
            hotel.setAmenities(amenities);
        }

        // Обработка файла обложки (если файл был загружен)
        if (coverImageFile != null && !coverImageFile.isEmpty()) {
            try {
                hotel.setCoverImage(coverImageFile.getBytes());  // Преобразуем MultipartFile в byte[]
            } catch (IOException e) {
                redirectAttributes.addFlashAttribute("error", "Error uploading cover image");
                return "redirect:/hotels/edit_full/" + id;
            }
        }
        // Удаление выбранных изображений
        if (deleteImageIds != null && !deleteImageIds.isEmpty()) {
            for (Long imageId : deleteImageIds) {
                imageService.deleteImage(imageId);
                logger.info("Изображение с ID {} было удалено", imageId);
            }
        }
        // Обработка дополнительных изображений (если они были загружены)
        if (imageFiles != null && !imageFiles.isEmpty()) {
            processImages(imageFiles, hotel);
        }

        // Сохранение обновленного отеля
        hotelService.save(hotel);

        // Перенаправляем обратно на список отелей партнёра
        return "redirect:/hotels/hotels_by_partner";
    }

    //Обработчик для отображения формы добавления отеля
    @GetMapping("/add")
    public String showAddHotelForm(HttpSession session, Model model) {
        // Создаем новый объект отеля
        model.addAttribute("hotel", new Hotel());

        // Добавляем список всех удобств
        model.addAttribute("amenities", amenityService.getAllAmenities());

        // Загружаем список городов
        List<City> cities = cityService.getAllCities();
        model.addAttribute("cities", cities);

        // Проверяем, авторизован ли партнер
        Partner loggedInPartner = (Partner) session.getAttribute("loggedInPartner");
        if (loggedInPartner == null) {
            return "redirect:/exit_Account";  // Перенаправляем на логин, если не авторизован
        }

        return "add_hotels";  // Возвращаем форму для регистрации отеля
    }
    // Обработчик запросов на отзывы
    @GetMapping("/{id}/reviews")
    public String showHotelReviews(@PathVariable("id") Long id, Model model) {
        Optional<Hotel> hotelOptional = hotelService.getHotelById(id);

        if (hotelOptional.isPresent()) {
            Hotel hotel = hotelOptional.get();
            model.addAttribute("hotel", hotel);
            model.addAttribute("reviews", hotel.getReviews()); // Передаем отзывы в модель
            return "reviews_partner"; // Это должно быть имя вашего Thymeleaf-шаблона
        } else {
            return "error/404"; // Страница ошибки, если отель не найден
        }

    }
    @GetMapping("/details/{id}")
    public String getPartnerHotelDetails(@PathVariable("id") Long id, Model model) {
        Optional<Hotel> hotelOptional = hotelService.getHotelById(id);  // Отримання готелю за ID

        if (hotelOptional.isPresent()) {
            Hotel hotel = hotelOptional.get();

            // Получаем список отзывов для данного отеля
            Set<Review> reviews = hotel.getReviews();

            // Рассчитываем средний рейтинг
            double averageRating = 0.0;
            if (!reviews.isEmpty()) {
                averageRating = reviews.stream()
                        .mapToDouble(Review::getRating)  // Извлекаем рейтинг из каждого отзыва
                        .average()  // Рассчитываем среднее значение
                        .orElse(0.0);  // Если отзывов нет, возвращаем 0.0
                averageRating = Math.round(averageRating * 10.0) / 10.0;  // Округляем до одного знака после запятой
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
    @GetMapping("/cover/{id}")
    @ResponseBody
    public ResponseEntity<byte[]> getHotelCover(@PathVariable Long id) {
        Hotel hotel = hotelService.getHotelById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid hotel Id: " + id));
        byte[] coverImage = hotel.getCoverImage();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.IMAGE_JPEG);

        return new ResponseEntity<>(coverImage, headers, HttpStatus.OK);
    }

//ДОДАВАННЯ НОВОГО ПОМЕШКАННЯ ПАРТНЕРОМ
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
                            @RequestParam(value = "coverUrl", required = false) String coverUrl,
                            @RequestParam(value = "coverImage", required = false) MultipartFile coverImageFile, // Вместо привязки к модели
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
        // Устанавливаем coverUrl
        hotel.setCoverUrl(coverUrl);

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
//        if (amenityIds != null && !amenityIds.isEmpty()) {
//            Set<Amenity> amenities = new HashSet<>(amenityService.getAllAmenitiesByIds(amenityIds));
//            hotel.setAmenities(amenities);
//        }
        // Привязываем удобства
        if (amenityIds != null) {
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

        // Сохраняем отель
        hotelService.save(hotel);
        logger.info("Отель {} был успешно сохранен с ID: {}", hotel.getName(), hotel.getId());

        // Обрабатываем дополнительные изображения с помощью выделенного метода
        if (imageFiles != null) {
            processImages(imageFiles, hotel);
        }
        return "redirect:/hotels/hotels_by_partner";
    }

    /**
     * Метод для обработки и сохранения изображений
     */
    private void processImages(List<MultipartFile> imageFiles, Hotel hotel) {
        for (MultipartFile imageFile : imageFiles) {
            if (!imageFile.isEmpty()) {
                try {
                    Image image = new Image();
                    image.setPhotoBytes(imageFile.getBytes());

                    // Устанавливаем URL для изображения
                    String imageUrl = "/images/" + imageFile.getOriginalFilename();
                    image.setUrl(imageUrl);

                    // Привязываем изображение к отелю
                    image.setHotel(hotel);

                    // Сохраняем изображение в базе данных
                    imageService.saveImage(image);
                    logger.info("Изображение {} сохранено для отеля ID: {}", imageFile.getOriginalFilename(), hotel.getId());

                } catch (IOException e) {
                    logger.error("Error processing image file: {}", e.getMessage());
                }
            } else {
                logger.warn("Пустой файл изображения: {}", imageFile.getOriginalFilename());
            }
        }
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

        // Получаем список отелей, принадлежащих партнеру
        List<Hotel> hotels = hotelService.getHotelsByOwner(loggedInPartner);

        // Логируем список отелей
        logger.info("Hotels retrieved: {}", hotels);

// Проверяем, есть ли отели, и передаем их в модель
        if (!hotels.isEmpty()) {
        // Для каждого отеля вычисляем средний рейтинг и добавляем отзывы
        for (Hotel hotel : hotels) {
            // Получаем отзывы для отеля
            Set<Review> reviews = hotel.getReviews();

            // Рассчитываем средний рейтинг
            double averageRating = 0.0;
            if (!reviews.isEmpty()) {
                averageRating = reviews.stream()
                        .mapToDouble(Review::getRating)
                        .average()
                        .orElse(0.0);
                averageRating = Math.round(averageRating * 10.0) / 10.0;
            }

            // Добавляем средний рейтинг в модель
            hotel.setAverageRating(averageRating);  // Допустим, у вас есть поле averageRating в классе Hotel

            // Получаем список удобств (amenities) для каждого отеля и добавляем в модель
            Set<Amenity> amenities = hotel.getAmenities();
            model.addAttribute("amenities", amenities);

            // Добавляем список отзывов в модель
            model.addAttribute("reviews", reviews);
        }

        // Добавляем список отелей в модель для отображения на странице
        model.addAttribute("hotels", hotels);
        } else {
            model.addAttribute("error", "No hotels found for this partner.");
        }

        // Возвращаем страницу hotels_by_partner.html
        return "hotels_by_partner";
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

    @DeleteMapping("/delete_hotels/{id}")
    public ResponseEntity<?> deleteHotel(@PathVariable Long id, HttpSession session) {
        Partner loggedInPartner = (Partner) session.getAttribute("loggedInPartner");
        if (loggedInPartner == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        boolean deleted = hotelService.deleteHotelById(id);
        if (deleted) {
            return ResponseEntity.ok().body("Hotel deleted");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Hotel not found");
        }
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

        Optional<City> cityOptional = cityService.getCityById(cityId);
        Optional<Partner> partnerOptional = partnerService.getPartnerById(partnerId);

        if (cityOptional.isPresent() && partnerOptional.isPresent()) {
            hotel.setCity(cityOptional.get());
            hotel.setOwner(partnerOptional.get());

            // Перетворення MultipartFile у байтовий масив
            if (!coverImageFile.isEmpty()) {
                byte[] coverImageBytes = coverImageFile.getBytes();
                hotel.setCoverImage(coverImageBytes);
            }

            hotelService.save(hotel);
            return "redirect:/hotels/hotel_list";
        } else {
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