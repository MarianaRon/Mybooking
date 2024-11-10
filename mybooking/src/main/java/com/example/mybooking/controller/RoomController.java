package com.example.mybooking.controller;

import com.example.mybooking.model.*;
import com.example.mybooking.service.*;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
    private ReservationService reservationService;

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

    // Получить номера, связанные с партнером
    @GetMapping("/room_by_partner/{hotelId}")
    public String getRoomsByPartner(@PathVariable("hotelId") Long hotelId, HttpSession session, Model model) {
        Partner loggedInPartner = (Partner) session.getAttribute("loggedInPartner");

        if (loggedInPartner == null) {
            return "redirect:/exit_Account";  // Перенаправить на страницу входа, если партнер не авторизован
        }

        // Получить отель по ID и проверить его владельца
        Hotel hotel = hotelService.getHotelById(hotelId)
                .orElseThrow(() -> new IllegalArgumentException("Некорректный ID отеля: " + hotelId));

        // Проверяем, что текущий партнер является владельцем отеля
        if (!hotel.getOwner().getId().equals(loggedInPartner.getId())) {
            return "redirect:/hotels/hotels_by_partner";  // Перенаправляем на список отелей партнера, если не владелец
        }

        // Получаем список номеров, связанных с отелем
        List<Room> rooms = roomService.getRoomsByHotel(hotelId);


        model.addAttribute("rooms", rooms);  // Передаем список номеров в модель
        model.addAttribute("hotel", hotel);  // Передаем информацию об отеле в модель

        return "room_by_partner";  // Возвращаем шаблон для отображения номеров
    }

    @GetMapping("/cover/{roomId}")
    @ResponseBody
    public ResponseEntity<byte[]> getRoomCover(@PathVariable Long roomId) {
        // Получаем комнату по её ID
        Room room = roomService.getRoomById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid room Id: " + roomId));

        // Получаем изображение обложки комнаты
        byte[] coverImage = room.getCoverImage();

        if (coverImage == null || coverImage.length == 0) {
            throw new IllegalArgumentException("Room does not have a cover image");
        }

        // Устанавливаем заголовки для комнати
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG); // Предположим, что изображение в формате JPEG

        // Возвращаем изображение с соответствующими заголовками
        return new ResponseEntity<>(coverImage, headers, HttpStatus.OK);
    }
    @GetMapping("/edit_full/{id}") // Изменяем маршрут для другого метода
    public String showEditRoomForm(@PathVariable("id") Long id, HttpSession session, Model model) {
        // Проверяем, авторизован ли партнер
        Partner loggedInPartner = (Partner) session.getAttribute("loggedInPartner");
        if (loggedInPartner == null) {
            return "redirect:/exit_Account";  // Перенаправляем на страницу логина
        }

        // Получаем номер по ID
        Room room = roomService.getRoomById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid room ID: " + id));

        // Проверяем, что партнёр является владельцем номера (через отель)
        if (!room.getHotel().getOwner().getId().equals(loggedInPartner.getId())) {
            return "redirect:rooms/room_by_partner/" + room.getHotel().getId();  // Перенаправляем, если партнёр не владелец
        }

        // Добавляем данные номера в модель для отображения
        model.addAttribute("room", room);

        // Добавляем список всех удобств для редактирования
        model.addAttribute("amenities", amenityService.getAllAmenities());

        // Добавляем текущие изображения номера (если есть)
        model.addAttribute("images", room.getImages());

        return "edit_full_room";  // Thymeleaf шаблон для редактирования данных номера
    }
    // Обработчик для сохранения изменений полной информации об номере

    @PostMapping("/edit_full/{id}")
    public String saveRoomEdit(@PathVariable("id") Long id,
                               @RequestParam("price") Double price,
                               @RequestParam("description") String description,
                               @RequestParam("capacity") Integer capacity,
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

        // Получаем номер по ID
        Room room = roomService.getRoomById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid room ID: " + id));

        // Проверяем, что партнёр является владельцем номера
        if (!room.getHotel().getOwner().getId().equals(loggedInPartner.getId())) {
            return "redirect:/rooms/room_by_partner/" + room.getHotel().getId();
        }
        // Выводим ID отеля в консоль для проверки
        System.out.println("Hotel ID: " + room.getHotel().getId());

        // Проверка, что capacity не null
        if (capacity == null || capacity <= 0) {
            redirectAttributes.addFlashAttribute("error", "Количество людей должно быть указано.");
            return "redirect:/rooms/edit_full/" + id;
        }
        // Обновляем данные номера

        room.setDescription(description);
        room.setPrice(price);
        room.setCapacity(capacity);

        // Привязываем удобства
        if (amenityIds != null) {
            Set<Amenity> amenities = new HashSet<>(amenityService.getAllAmenitiesByIds(amenityIds));
            room.setAmenities(amenities);
        }

        // Обработка файла обложки (если файл был загружен)
        if (coverImageFile != null && !coverImageFile.isEmpty()) {
            try {
                room.setCoverImage(coverImageFile.getBytes());  // Преобразуем MultipartFile в byte[]
            } catch (IOException e) {
                redirectAttributes.addFlashAttribute("error", "Error uploading cover image");
                return "redirect:/rooms/edit_full/" + id;
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
            processImages(imageFiles, room);
        }

        // Сохранение обновленного номера
        roomService.saveRoom(room);

        // Перенаправляем обратно на список номеров партнёра
        return "redirect:/rooms/room_by_partner/" + room.getHotel().getId();
    }

//    @GetMapping("/rooms/{id}/reservations")
//    public String viewRoomReservations(@PathVariable("id") Long roomId, Model model, HttpSession session) {
//        // Получаем текущего авторизованного партнёра
//        Partner loggedInPartner = (Partner) session.getAttribute("loggedInPartner");
//        if (loggedInPartner == null) {
//            return "redirect:/exit_Account";  // Перенаправляем на страницу входа, если партнёр не авторизован
//        }
//
//        // Получаем номер по ID
//        Room room = roomService.getRoomById(roomId)
//                .orElseThrow(() -> new IllegalArgumentException("Invalid room ID: " + roomId));
//
//        // Проверяем, что партнёр является владельцем комнаты
//        if (!room.getHotel().getOwner().getId().equals(loggedInPartner.getId())) {
//            return "redirect:/hotels/hotels_by_partner";
//        }
//
//        // Получаем все бронирования для данной комнаты
//        List<Reservation> reservations = reservationService.getReservationsByRoom(roomId);
//
//        // Добавляем комнату и бронирования в модель
//        model.addAttribute("room", room);
//        model.addAttribute("reservations", reservations);
//
//        return "view_reservations";  // Возвращаем шаблон для отображения бронирований
//    }
// Метод для отображения всех бронирований определенного номера
@GetMapping("/{roomId}/reservations")
public String getRoomReservations(@PathVariable Long roomId, Model model, HttpSession session) {
    // Получаем текущего авторизованного партнера
    Partner loggedInPartner = (Partner) session.getAttribute("loggedInPartner");
    if (loggedInPartner == null) {
        return "redirect:/exit_Account"; // Перенаправляем на страницу входа, если партнер не авторизован
    }

    // Получаем номер по ID
    Room room = roomService.getRoomById(roomId)
            .orElseThrow(() -> new IllegalArgumentException("Invalid room ID: " + roomId));

    // Проверяем, что партнёр является владельцем номера
    if (!room.getHotel().getOwner().getId().equals(loggedInPartner.getId())) {
        return "redirect:/hotels/hotels_by_partner"; // Перенаправляем, если партнёр не владелец номера
    }

    // Получаем список бронирований для данного номера
    List<Reservation> reservations = reservationService.getReservationsByRoom(roomId);

    // Логирование для отладки
    logger.info("Total reservations found for room ID {}: {}", roomId, reservations.size());

    // Добавляем данные в модель
    model.addAttribute("room", room);
    model.addAttribute("reservations", reservations);

    return "room_reservations"; // Возвращаем шаблон для отображения бронирований
}

    @PostMapping("/deleteRoom/{id}")
    public String deleteRoomPartner(@PathVariable("id") Long roomId, HttpSession session, RedirectAttributes redirectAttributes) {
        // Получаем текущего авторизованного партнера
        Partner loggedInPartner = (Partner) session.getAttribute("loggedInPartner");

        // Проверяем, авторизован ли партнёр
        if (loggedInPartner == null) {
            return "redirect:/exit_Account";  // Перенаправляем на страницу входа, если партнёр не авторизован
        }
        // Получаем номер по ID
        Room room = roomService.getRoomById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Неправильный ID номера: " + roomId));
        // Проверяем, что партнёр является владельцем отеля, к которому относится номер
        if (!room.getHotel().getOwner().getId().equals(loggedInPartner.getId())) {
            return "redirect:/exit_Account";  // Перенаправляем, если партнёр не владелец номера
        }
        // Удаляем номер
        roomService.deleteRoomById(roomId, loggedInPartner.getId());
        // Добавляем сообщение об успешном удалении
        redirectAttributes.addFlashAttribute("success", "Номер успешно удалён.");
        // Перенаправляем на список номеров партнёра
        return "redirect:/rooms/room_by_partner/" + room.getHotel().getId();
    }

}
