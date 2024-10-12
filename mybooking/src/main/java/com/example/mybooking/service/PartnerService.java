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
    @Transactional
    public void updatePartner(Partner partner) {
        partnerRepository.save(partner); // Используйте метод save для обновления партнера
    }
    public Optional<Partner> findByEmail(String email) {
        return partnerRepository.findByEmail(email);
    }
}
