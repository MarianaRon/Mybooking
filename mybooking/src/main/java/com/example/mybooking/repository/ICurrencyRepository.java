package com.example.mybooking.repository;


import com.example.mybooking.model.Currency;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ICurrencyRepository extends JpaRepository<Currency, Long> {

}
