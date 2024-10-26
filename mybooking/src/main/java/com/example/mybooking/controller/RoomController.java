package com.example.mybooking.controller;

import com.example.mybooking.model.*;
import com.example.mybooking.service.AmenityService;
import com.example.mybooking.service.HotelService;
import com.example.mybooking.service.ImageService;
import com.example.mybooking.service.RoomService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Controller
@RequestMapping("/rooms")
public class RoomController {
   @Autowired
    private RoomService roomService;
    @Autowired
    private HotelService hotelService;

    @Autowired
    private AmenityService amenityService;
    @Autowired
    private ImageService imageService;
    private static final Logger logger = LoggerFactory.getLogger(RoomController.class);
    @GetMapping("/room_list")
    public String listRooms(Model model) {
        model.addAttribute("rooms", roomService.getAllRooms());
        model.addAttribute("hotels", hotelService.getAllHotels()); // Передаємо список готелів для форми
        return "rooms/room_list";
    }

    @GetMapping("/edit/{id}")
    public String editRoomForm(@PathVariable Long id, Model model) {
        Room room = roomService.getRoomById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid room Id: " + id));
        model.addAttribute("room", room);
        model.addAttribute("hotels", hotelService.getAllHotels()); // Передаємо список готелів для редагування
        return "rooms/edit_room";
    }

    @PostMapping("/edit/{id}")
    public String updateRoom1(@PathVariable Long id, @ModelAttribute Room roomDetails) {
        Room room = roomService.getRoomById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid room Id: " + id));

        room.setType(roomDetails.getType());
        room.setPrice(roomDetails.getPrice());
        room.setCapacity(roomDetails.getCapacity());
        room.setHotel(roomDetails.getHotel());

        roomService.saveRoom(room);
        return "redirect:/rooms/room_list";
    }

    @PostMapping("/delete/{id}")
    public String deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return "redirect:/rooms/room_list";
    }
    // Обработка данных формы и сохранение номера
    @PostMapping("/add")
    public String addRoom(@RequestParam String type,
                          @RequestParam Double price,
                          @RequestParam Integer capacity,
                          @RequestParam Long hotelId) {
        Hotel hotel = hotelService.getHotelById(hotelId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid hotel Id: " + hotelId));
        Room room = new Room(type, price, capacity, hotel);
        roomService.saveRoom(room);
        return "redirect:/rooms/room_list";
    }

    @GetMapping
    public String getAllRooms(Model model) {
        model.addAttribute("rooms", roomService.getAllRooms());
        return "rooms/room_list";
    }

    @GetMapping("/{id}")
    public ResponseEntity<Room> getRoomById(@PathVariable Long id) {
        Optional<Room> room = roomService.getRoomById(id);
        return room.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Room> createRoom(@RequestBody Room room) {
        Room savedRoom = roomService.saveRoom(room);
        return ResponseEntity.ok(savedRoom);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Room> updateRoom2(@PathVariable Long id, @RequestBody Room roomDetails) {
        Room room = roomService.getRoomById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid room Id: " + id));

        room.setType(roomDetails.getType());
        room.setPrice(roomDetails.getPrice());
        room.setCapacity(roomDetails.getCapacity());
        room.setHotel(roomDetails.getHotel());
        room.setReservations(roomDetails.getReservations());
        room.setImages(roomDetails.getImages());

        Room updatedRoom = roomService.saveRoom(room);
        return ResponseEntity.ok(updatedRoom);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoomRest(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }

    ///////////////////////////
    // Отображение формы для добавления номера
//    @GetMapping("/add_room/{hotelId}")
//    public String showAddRoomForm(@PathVariable("hotelId") Long hotelId, Model model) {
//        model.addAttribute("room", new Room());
//        model.addAttribute("hotelId", hotelId); // Передача ID отеля в форму
//        return "add_room"; // возвращает на страницу add_room.html
//    }


//    перехід на сторінку з описом кімнати
    @GetMapping("/roomDetails/{roomId}")
    public String getRoomDetails(@PathVariable("roomId") Long roomId, Model model, HttpSession session) {
        Optional<Room> roomOpt = roomService.getRoomById(roomId);

        if (roomOpt.isPresent()) {
            Room room = roomOpt.get();
            model.addAttribute("room", room);

            // Отримуємо готель, до якого належить кімната
            Hotel hotel = room.getHotel();
            model.addAttribute("hotel", hotel);

            // Додаємо поточного користувача в модель, якщо він є
            User user = (User) session.getAttribute("currentUser");
            model.addAttribute("currentUser", user);

            return "rooms/roomDetails";
        } else {
            return "redirect:/error";
        }
    }

//////////////////////////////////////room
@GetMapping("/add/{hotelId}")
public String showAddRoomForm(@PathVariable("hotelId") Long hotelId, Model model) {
    // Проверяем, существует ли отель с данным ID
    Optional<Hotel> hotelOptional = hotelService.getHotelById(hotelId);

    if (hotelOptional.isEmpty()) {
        // Если отель не найден, перенаправляем на страницу ошибки или списка отелей
        return "redirect:/hotels?error=hotel_not_found";
    }

    // Если отель найден, передаем информацию в модель
    Hotel hotel = hotelOptional.get();

    // Передаем ID отеля в модель
    model.addAttribute("hotelId", hotelId);

    // Передаем объект отеля в модель (если требуется для формы)
    model.addAttribute("hotel", hotel);

    // Добавляем список всех удобств в модель
    model.addAttribute("amenities", amenityService.getAllAmenities());

    // Передаем новый пустой объект Room, который будет заполняться на форме
    model.addAttribute("room", new Room());

    // Возвращаем страницу с формой добавления комнаты
    return "add_room_partner"; // Имя HTML страницы
}

    @PostMapping("/partnerroomadd")
    public String addRoomPartner(//HttpSession session,
                                 @RequestParam("hotelId") Long hotelId,
                                 @RequestParam(value = "price", required = false) Double price,
                                 @RequestParam("description") String description,
                                 @RequestParam("type") String type,
                                 @RequestParam("capacity") Integer capacity,
                                 @RequestParam(value = "amenities", required = false) List<Long> amenityIds,
                                 @RequestParam(value = "coverUrl", required = false) String coverUrl,
                                 @RequestParam(value = "coverImage", required = false) MultipartFile coverImageFile,
                                 @RequestParam(value = "imageFiles", required = false) List<MultipartFile> imageFiles) {

        // Логируем полученные данные для отладки
        logger.info("Received hotelId: " + hotelId);
        logger.info("Received price: " + price);
        logger.info("Received description: " + description);
        logger.info("Received type: " + type);
        logger.info("Received capacity: " + capacity);

        // Установка основных данных для номера
        Room room = new Room();
        room.setPrice(price);
        room.setDescription(description);
        room.setType(type);
        room.setCapacity(capacity);
        room.setCoverUrl(coverUrl);
        // Если `Room` ссылается на объект `Hotel`, передаем объект `Hotel`
        Optional<Hotel> hotelOptional = hotelService.getHotelById(hotelId);
        if (hotelOptional.isPresent()) {
            Hotel hotel = hotelOptional.get();
            room.setHotel(hotel);  // Устанавливаем объект отеля в номер
        }
        // Получаем текущего авторизованного партнера
//        Partner loggedInPartner = (Partner) session.getAttribute("loggedInPartner");
//        if (loggedInPartner == null) {
//            logger.error("Partner not found in session. Redirecting to login.");
//            return "redirect:/partner_Account";
//        }
//        // Устанавливаем партнера как владельца отеля
//        room.setOwner(loggedInPartner);


        // Привязываем удобства, если они есть
        if (amenityIds != null) {
            Set<Amenity> amenities = new HashSet<>(amenityService.getAllAmenitiesByIds(amenityIds));
            room.setAmenities(amenities);
        }


        // Проверяем, если coverImage не пустое
        if (coverImageFile != null && !coverImageFile.isEmpty()) {
            try {
                room.setCoverImage(coverImageFile.getBytes());
            } catch (IOException e) {
                // Обрабатываем ошибку загрузки
                logger.error("Ошибка при загрузке обложки: " + e.getMessage());
                return "redirect:/rooms/add?error=cover_image_upload_failed";
            }
        } else {
            // Если изображение не предоставлено, устанавливаем поле в null
            room.setCoverImage(null);  // Это возможно только если база данных позволяет null
        }

        // Сохраняем номер в отеле
        roomService.saveRoomPartner(room, hotelId);

        // Обработка изображений для номера
        if (imageFiles != null && !imageFiles.isEmpty()) {
            processImages(imageFiles, room);
        }

        return "redirect:/hotels/hotels_by_partner";  // Перенаправляем пользователя на страницу партнерских отелей
    }

    /**
     * Метод для обработки и сохранения изображений
     */
    private void processImages(List<MultipartFile> imageFiles, Room room) {
        for (MultipartFile imageFile : imageFiles) {
            if (!imageFile.isEmpty()) {
                try {
                    Image image = new Image();
                    image.setPhotoBytes(imageFile.getBytes());

                    // Устанавливаем URL для изображения
                    String imageUrl = "/images/" + imageFile.getOriginalFilename();
                    image.setUrl(imageUrl);

                    // Привязываем изображение к номеру
                    image.setRoom(room);

                    // Сохраняем изображение в базе данных
                    imageService.saveImage(image);
                    logger.info("Изображение {} сохранено для номера ID: {}", imageFile.getOriginalFilename(), room.getId());

                } catch (IOException e) {
                    logger.error("Error processing image file: {}", e.getMessage());
                }
            } else {
                logger.warn("Пустой файл изображения: {}", imageFile.getOriginalFilename());
            }
        }
    }
}
