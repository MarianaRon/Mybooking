package com.example.mybooking.service;

import com.example.mybooking.controller.RoomController;
import com.example.mybooking.model.Hotel;
import com.example.mybooking.model.Room;
import com.example.mybooking.repository.IHotelRepository;
import com.example.mybooking.repository.IRoomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RoomService {

    @Autowired
    private IRoomRepository roomRepository;

    @Autowired
    private IHotelRepository hotelRepository;
    private static final Logger logger = LoggerFactory.getLogger(RoomService.class);

    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public Optional<Room> getRoomById(Long id) {
        return roomRepository.findById(id);
    }

    public Optional<Room> get_RoomById(Long id) {
        return roomRepository.findById(id);
    }

    public Room saveRoom(Room room) {
        return roomRepository.save(room);
    }

    public void saveRoomPartner(Room room, Long hotelId) {
        // Получаем отель по ID, выбрасываем свое исключение, если отель не найден
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new HotelNotFoundException("Hotel with ID " + hotelId + " not found"));

        // Устанавливаем отель для комнаты
        room.setHotel(hotel);

        // Логируем процесс для отладки
        logger.info("Saving room for hotel ID: {}", hotelId);

        // Сохраняем комнату
        roomRepository.save(room);
    }
    public class HotelNotFoundException extends RuntimeException {
        public HotelNotFoundException(String message) {
            super(message);
        }
    }

    public void deleteRoomById(Long roomId, Long partnerId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Неправильный ID комнаты: " + roomId));

        if (!room.getHotel().getOwner().getId().equals(partnerId)) {
            throw new SecurityException("Партнёр не является владельцем этой комнаты.");
        }

        roomRepository.deleteById(roomId);
    }

    public void deleteRoom(Long id) {
        roomRepository.deleteById(id);
    }

    // Добавляем метод для получения номеров по списку идентификаторов
    public List<Room> getRoomsByIds(List<Long> roomIds) {
        return roomRepository.findAllById(roomIds);
    }

    public List<Room> getRoomsByHotel(Long hotelId) {
        return roomRepository.findByHotelId(hotelId);
    }

}