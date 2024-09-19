package com.example.mybooking.service;

import com.example.mybooking.model.Room;
import com.example.mybooking.repository.IRoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RoomService {

    @Autowired
    private IRoomRepository roomRepository;

    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public Optional<Room> getRoomById(Long id) {
        return roomRepository.findById(id);
    }

    public Room saveRoom(Room room) {
        return roomRepository.save(room);
    }

    public void deleteRoom(Long id) {
        roomRepository.deleteById(id);
    }
    // Добавляем метод для получения номеров по списку идентификаторов
    public List<Room> getRoomsByIds(List<Long> roomIds) {
        return roomRepository.findAllById(roomIds);
    }
}