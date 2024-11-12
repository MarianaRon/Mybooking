package com.example.mybooking.service;


import com.example.mybooking.model.Partner;
import com.example.mybooking.repository.IPartnerRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;

@Service
public class PartnerService {

    @Autowired
    private IPartnerRepository partnerRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();


    public PartnerService(IPartnerRepository partnerRepository) {
        this.partnerRepository = partnerRepository;
    }

    public List<Partner> getAllPartners() {
        return partnerRepository.findAll();
    }

    public Optional<Partner> getPartnerById(Long id) {

        return partnerRepository.findById(id);
    }

    public Partner createPartner(Partner partner) {

        return partnerRepository.save(partner);
    }

    public void deletePartner(Long id) {

        partnerRepository.deleteById(id);
    }

    // Обновление профиля партнера
    public String updatePartnerProfile(Partner existingPartner, Partner updatedPartner, String newPassword, String confirmPassword) {

        // Обновляем личные данные
        existingPartner.setFirstName(updatedPartner.getFirstName());
        existingPartner.setLastName(updatedPartner.getLastName());
        existingPartner.setEmail(updatedPartner.getEmail());
        existingPartner.setPhone(updatedPartner.getPhone());

        // Логика изменения пароля (если введен новый пароль)
        if (newPassword != null && !newPassword.isEmpty()) {
            if (!newPassword.equals(confirmPassword)) {
                return "Паролі не співпадають";
            }
            if (newPassword.length() < 6) {
                return "Пароль має бути не меньше 6 символів";
            }
            // Хешируем новый пароль перед сохранением
            String hashedPassword = passwordEncoder.encode(newPassword);
            existingPartner.setPassword(hashedPassword);
        }

        // Сохраняем обновленные данные партнера
        partnerRepository.save(existingPartner);

        return null; // Возвращаем null, если все прошло успешно
    }
    public Optional<Partner> findByEmail(String email) {
        return partnerRepository.findByEmail(email);
    }


}
