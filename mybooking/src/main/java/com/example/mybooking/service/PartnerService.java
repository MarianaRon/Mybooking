package com.example.mybooking.service;


import com.example.mybooking.model.Partner;
import com.example.mybooking.repository.IPartnerRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PartnerService {

    @Autowired
    private IPartnerRepository partnerRepository;

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
                return "Пароли не совпадают";
            }
            if (newPassword.length() < 6) {
                return "Пароль должен быть длиной не менее 6 символов";
            }
            existingPartner.setPassword(newPassword); // Обновляем пароль
        }

        // Сохраняем обновленные данные партнера
        partnerRepository.save(existingPartner);

        return null; // Возвращаем null, если все прошло успешно
    }
    public Optional<Partner> findByEmail(String email) {
        return partnerRepository.findByEmail(email);
    }
}
