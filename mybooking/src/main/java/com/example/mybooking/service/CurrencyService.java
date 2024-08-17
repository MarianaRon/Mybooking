package com.example.mybooking.service;


import com.example.mybooking.model.Currency;
import com.example.mybooking.repository.ICurrencyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service // Обозначает, что этот класс является сервисом Spring
public class CurrencyService {

    @Autowired // Автоматическая настройка зависимости
    private ICurrencyRepository currencyRepository;

    // Метод для получения всех валют из базы данных
    public List<Currency> getAllCurrencies() {
        return currencyRepository.findAll();
    }

    // Метод для сохранения новой валюты в базе данных
    public void saveCurrency(Currency currency) {
        currencyRepository.save(currency);
    }
}
