package com.example.mybooking.repository;

import com.example.mybooking.model.Partner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IPartnerRepository extends JpaRepository<Partner, Long> {
    Optional<Partner> findByEmail(String email);
}