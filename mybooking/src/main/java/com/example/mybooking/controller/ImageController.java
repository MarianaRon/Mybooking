package com.example.mybooking.controller;

import com.example.mybooking.model.Hotel;
import com.example.mybooking.model.Image;
import com.example.mybooking.model.Room;
import com.example.mybooking.service.HotelService;
import com.example.mybooking.service.ImageService;
import com.example.mybooking.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Controller
@RequestMapping("/images")
public class ImageController {
    @Autowired
    private HotelService hotelService;  // Инжектируем HotelService

    @Autowired
    private RoomService roomService;
    @Autowired
    private ImageService imageService;

    @GetMapping("/image_list")
    public String imageList(Model model) {
        List<Image> images = imageService.getAllImages();
        model.addAttribute("images", images);
        return "images/image_list";
    }

    @PostMapping("/add")
    public String createImage(@RequestParam("url") String url,
                              @RequestParam("photoBytes") MultipartFile photoBytes,
                              @RequestParam("hotel_id") Long hotelId,
                              @RequestParam(value = "room_id", required = false) Long roomId) {
        Image image = new Image();
        image.setUrl(url);

        // Обробка байтових даних зображення
        if (!photoBytes.isEmpty()) {
            try {
                image.setPhotoBytes(photoBytes.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
//        /////////////////////////////
//        // Найти отель по hotelId
//        Optional<Hotel> hotelOptional = hotelService.getHotelById(hotelId);
//        if (hotelOptional.isPresent()) {
//            // Устанавливаем отель для изображения
//            Hotel hotel = hotelOptional.get();
//            image.setHotel(hotel);
//        } else {
//            // Обработка ошибки, если отель не найден
//            return "redirect:/hotels/add?error=hotel_not_found";
//        }

        // Зв'язок з готелем
        Optional<Hotel> hotelOptional = hotelService.getHotelById(hotelId);
        if (hotelOptional.isPresent()) {
            Hotel hotel = hotelOptional.get();
            image.setHotel(hotel);
        } else {
            return "redirect:/hotels/add?error=hotel_not_found";
        }

        // Зв'язок з кімнатою (якщо передана)
        if (roomId != null) {
            Optional<Room> roomOptional = roomService.getRoomById(roomId);
            if (roomOptional.isPresent()) {
                Room room = roomOptional.get();
                image.setRoom(room);
            }
        }

        // Збереження зображення
        imageService.saveImage(image);
        return "redirect:/images/image_list";
    }

    @PostMapping("/edit/{id}")
    public String updateImage(@PathVariable Long id,
                              @RequestParam("url") String url,
                              @RequestParam("photoBytes") MultipartFile photoBytes,
                              @RequestParam("hotel_id") Long hotelId,
                              @RequestParam(value = "room_id", required = false) Long roomId) {
        Optional<Image> optionalImage = imageService.getImageById(id);
        if (optionalImage.isPresent()) {
            Image image = optionalImage.get();
            image.setUrl(url);

            // Оновлення байтових даних зображення
            if (!photoBytes.isEmpty()) {
                try {
                    image.setPhotoBytes(photoBytes.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            imageService.saveImage(image);
        }
        return "redirect:/images/image_list";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Optional<Image> optionalImage = imageService.getImageById(id);
        if (optionalImage.isPresent()) {
            Image image = optionalImage.get();
            model.addAttribute("image", image);
            return "images/edit_image"; // Ім'я шаблону редагування
        }
        return "redirect:/images/image_list";
    }

    @GetMapping("/photo/{id}")
    @ResponseBody
    public ResponseEntity<byte[]> getImagePhoto(@PathVariable Long id) {
        Image image = imageService.getImageById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid image Id: " + id));

        byte[] photoBytes = image.getPhotoBytes();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.IMAGE_JPEG);
        return new ResponseEntity<>(photoBytes, headers, HttpStatus.OK);
    }


    @PostMapping("/{id}/delete")
    public String deleteImage(@PathVariable Long id) {
        imageService.deleteImage(id);
        return "redirect:/images/image_list";
    }

}
