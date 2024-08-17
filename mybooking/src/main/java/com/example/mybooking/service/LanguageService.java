package com.example.mybooking.service;

import com.example.mybooking.model.Language;
import com.example.mybooking.repository.ILanguageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Сервис для управления логикой выбора языка.
 */
@Service
public class LanguageService {

    @Autowired
    private ILanguageRepository languageRepository;

    /**
     * Получить все доступные языки.
     *
     * @return список языков
     */
    public List<Language> getAllLanguages() {
        return languageRepository.findAll();
    }
}