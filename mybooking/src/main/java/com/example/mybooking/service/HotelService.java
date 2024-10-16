package com.example.mybooking.service;

import com.example.mybooking.model.Amenity;
import com.example.mybooking.model.Hotel;
import com.example.mybooking.model.Image;
import com.example.mybooking.model.Partner;
import com.example.mybooking.repository.IAmenityRepository;
import com.example.mybooking.repository.IHotelRepository;
import com.example.mybooking.repository.IImageRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

//import static jdk.nio.zipfs.ZipFileAttributeView.AttrID.owner;

@Service
public class HotelService {

    private static final Logger logger = LoggerFactory.getLogger(HotelService.class);
    @Autowired
    private IImageRepository imageRepository;
    @Autowired
    private IHotelRepository hotelRepository;

    @Autowired
    private IAmenityRepository amenityRepository;  // Инжектируем AmenityRepository


    //    /**
//     * Получение всех отелей из базы данных.
//     *
//     * @return список всех отелей.
//     */
    public List<Hotel> getAllHotels() {
        logger.debug("Fetching all hotels");
        return hotelRepository.findAll();
    }


    //для роботи з картою на головній сторінці
    public List<Hotel> get_AllHotels() {
        logger.debug("Fetching all hotels");
        List<Hotel> hotels = hotelRepository.findAll();
        logger.debug("Hotels found: " + hotels.size()); // Додаємо лог для перевірки
        return hotels;
    }

    /**
     * Получение отеля по ID.
     *
     * @param id ID отеля
     * @return Optional с отелем, если найден
     */
    public Optional<Hotel> getHotelById(Long id) {
        logger.debug("Fetching hotel with ID: {}", id);
        return hotelRepository.findById(id);
    }

    /**
     * Сохранение отеля вместе с привязкой к партнеру.
     *
     * @param hotel объект отеля
     * @param partner объект партнера
     * @return сохранённый объект отеля
     */


    ////////////////////////////////
//    @Transactional
//    public Hotel saveHotelWithPartner(Hotel hotel, Partner partner, List<Long> amenityIds,
//                                      MultipartFile coverImageFile, Set<MultipartFile> imageFiles) {
//
//        // Привязываем партнера к отелю
//        hotel.setOwner(partner);
//
//        // Привязываем удобства к отелю по списку их ID
//        if (amenityIds != null && !amenityIds.isEmpty()) {
//            // Получаем объекты удобств из базы данных по их ID
//            Set<Amenity> amenities = new HashSet<>(amenityRepository.findAllById(amenityIds));
//            // Устанавливаем удобства для отеля
//            hotel.setAmenities(amenities);
//            logger.info("Удобства добавлены к отелю: {}", amenities);
//        } else {
//            logger.warn("Удобства не были добавлены, так как список был пуст.");
//        }
//
//        // Логирование временной директории
//        logger.info("Временная директория для хранения файлов: {}", System.getProperty("java.io.tmpdir"));
//
//
//        // Сохраняем обложку
//        if (coverImageFile != null && !coverImageFile.isEmpty()) {
//            try {
//                // Преобразуем обложку в массив байтов
//                byte[] coverImageBytes = coverImageFile.getBytes();
//                hotel.setCoverImage(coverImageBytes); // Сохраняем в поле coverImage
//                logger.info("Обложка сохранена для отеля: {}", hotel.getName());
//            } catch (IOException e) {
//                logger.error("Ошибка при обработке файла обложки: " + e.getMessage());
//                throw new RuntimeException("Ошибка при загрузке обложки", e);
//            }
//        }
//
//        // Обрабатываем дополнительные изображения
//        if (imageFiles != null && !imageFiles.isEmpty()) {
//            Set<Image> images = new HashSet<>();
//            for (MultipartFile file : imageFiles) {
//                if (!file.isEmpty()) {
//                    try {
//                        Image image = new Image();
//                        image.setPhotoBytes(file.getBytes());
//                        image.setHotel(hotel);
//                        images.add(image);
//
//                        // Логирование пути сохранения временного файла
//                        logger.info("Временный файл для изображения: {}", file.getOriginalFilename());
//                        // Проверка существования временного файла перед обработкой
//                        String tempFilePath = System.getProperty("java.io.tmpdir") + "/" + file.getOriginalFilename();
//                        if (Files.exists(Paths.get(tempFilePath))) {
//                            logger.info("Файл найден в временной директории: {}", tempFilePath);
//                        } else {
//                            logger.error("Файл не найден в временной директории: {}", tempFilePath);
//                        }
//
//                        logger.info("Дополнительное изображение добавлено: {}", file.getOriginalFilename());
//                    } catch (IOException e) {
//                        logger.error("Ошибка при обработке дополнительного изображения: {}", e.getMessage());
//                    }
//                }
//            }
//            hotel.setImages(images);
//        } else {
//            logger.warn("Дополнительные изображения не были загружены.");
//        }
//
//        // Сохраняем отель в базе данных
//        try {
//            return hotelRepository.save(hotel);
//        } catch (Exception e) {
//            logger.error("Ошибка при сохранении отеля: {}", e.getMessage());
//            throw e;
//        }
    @Transactional
    public Hotel saveHotelWithPartner(Hotel hotel) {
        return hotelRepository.save(hotel);
        //        // Привязываем партнера к отелю
//        hotel.setOwner(partner);
//
//        // Добавляем удобства, если список не пуст
//        if (amenityIds != null && !amenityIds.isEmpty()) {
//            Set<Amenity> amenities = new HashSet<>(amenityRepository.findAllById(amenityIds));
//            hotel.setAmenities(amenities);
//            logger.info("Удобства добавлены к отелю: {}", amenities);
//        } else {
//            logger.warn("Удобства не были добавлены, так как список был пуст.");
//        }
//
//        // Обработка обложки отеля
//        if (coverImageFile != null && !coverImageFile.isEmpty()) {
//            try {
//                byte[] coverImageBytes = coverImageFile.getBytes();
//                hotel.setCoverImage(coverImageBytes);
//                hotel.setCoverUrl(null); // Если используется байтовое изображение, обнуляем URL
//                logger.info("Изображение обложки сохранено для отеля: {}", hotel.getName());
//            } catch (IOException e) {
//                logger.error("Ошибка при сохранении изображения обложки: {}", e.getMessage());
//                throw new RuntimeException("Не удалось сохранить изображение обложки", e);
//            }
//        } else {
//            logger.warn("Изображение обложки не было предоставлено.");
//        }
//
//        // Сохраняем отель в базе данных
//        Hotel savedHotel = hotelRepository.save(hotel);
//
//        // Обработка дополнительных изображений отеля, если они есть
//        if (imageFiles != null && !imageFiles.isEmpty()) {
//            for (MultipartFile imageFile : imageFiles) {
//                if (!imageFile.isEmpty()) {
//                    try {
//                        byte[] imageBytes = imageFile.getBytes();
//                        Image image = new Image();
//                        image.setPhotoBytes(imageBytes);
//                        image.setHotel(savedHotel);
//                        imageRepository.save(image);
//                        logger.info("Дополнительное изображение сохранено для отеля: {}", savedHotel.getName());
//                    } catch (IOException e) {
//                        logger.error("Ошибка при сохранении дополнительного изображения: {}", e.getMessage());
//                    }
//                }
//            }
//        } else {
//            logger.warn("Дополнительные изображения не были предоставлены.");
//        }
//
//        return savedHotel;
    }
    /**
     * Удаление отеля по ID.
     *
     * @param id ID отеля для удаления
     */
    public boolean deleteHotelById(Long id) {
        logger.debug("Attempting to delete hotel with ID: {}", id);

        if (hotelRepository.existsById(id)) {
            hotelRepository.deleteById(id);
            logger.info("Hotel with ID: {} successfully deleted", id);
            return true;
        } else {
            logger.warn("Hotel with ID: {} not found", id);
            return false;
        }
    }

    /**
     * Поиск отелей по названию или описанию.
     *
     * @param searchTerm поисковый запрос
     * @return список отелей, соответствующих запросу
     */
    public List<Hotel> searchHotelsByNameOrDescription(String searchTerm) {
        logger.debug("Searching for hotels by name or description with term: {}", searchTerm);
        return hotelRepository.findByNameContainingOrDescriptionContaining(searchTerm, searchTerm);
    }

    /**
     * Получение всех отелей, принадлежащих партнеру.
     *
     * @param owner объект партнера (владельца)
     * @return список отелей, принадлежащих партнеру
     */
    public List<Hotel> getHotelsByOwner(Partner owner) {
        logger.debug("Fetching hotels for partner: {}", owner.getId());
        return hotelRepository.findByOwner(owner);
    }


    /**
     * Обновление существующего отеля.
     *
     * @param id ID отеля для обновления
     * @param hotelDetails объект с новыми данными для отеля
     */
    public void updateHotel(Long id, Hotel hotelDetails) {
        hotelRepository.findById(id).ifPresent(hotel -> {
            logger.debug("Updating hotel with ID: {}", id);
            hotel.setName(hotelDetails.getName());
            hotel.setDescription(hotelDetails.getDescription());
            hotel.setLatitude(hotelDetails.getLatitude());
            hotel.setLongitude(hotelDetails.getLongitude());
            hotel.setOwner(hotelDetails.getOwner());
            hotel.setHousingType(hotelDetails.getHousingType());
            hotelRepository.save(hotel);
            logger.info("Hotel with ID: {} successfully updated", id);
        });
    }

    //для виведення готелей по місту
    // Метод для збереження готелю
    public void save(Hotel hotel) {
        hotelRepository.save(hotel);
    }
}