package com.example.mybooking.config;


import com.example.mybooking.model.Currency;
import com.example.mybooking.model.Language;
import com.example.mybooking.repository.ICurrencyRepository;
import com.example.mybooking.repository.ILanguageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Компонент для инициализации базы данных при запуске приложения.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private ICurrencyRepository currencyRepository;
    @Autowired
    private ILanguageRepository languageRepository;


    @Override
    public void run(String... args) throws Exception {
         //Список валют для инициализации
//        List<Currency> currencies = Arrays.asList(
//                new Currency("USD", "US Dollar"),
//                new Currency("EUR", "Euro"),
//                new Currency("GBP", "British Pound"),
//                new Currency("JPY", "Japanese Yen"),
//                new Currency("UAH", "Ukrainian Hryvnia")
//        );

        // Сохранение валют в базе данных
      // currencyRepository.saveAll(currencies);

        // Список языков для инициализации
//        List<Language> languages = Arrays.asList(
//                new Language("UK", "Ukrainian"),
//                new Language("EN", "English"),
//                new Language("RU", "Русский"),
//                new Language("FR", "Français"),
//                new Language("DE", "Deutsch"),
//                new Language("ES", "Español")
//        );

        // Сохранение языков в базе данных
     //   languageRepository.saveAll(languages);
    }
}