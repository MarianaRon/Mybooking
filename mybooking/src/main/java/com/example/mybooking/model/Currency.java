package com.example.mybooking.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity // Обозначает, что этот класс является сущностью JPA
public class Currency {

    @Id // Обозначает первичный ключ
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Автоматическая генерация значения первичного ключа
    private Long id;

    private String code; // Код валюты (например, USD, EUR)
    private String name; // Название валюты (например, Доллар США, Евро)

    // Конструктор по умолчанию
    public Currency() {
    }

    // Конструктор с параметрами
    public Currency(String code, String name) {
        this.code = code;
        this.name = name;
    }

    // Геттер для id
    public Long getId() {
        return id;
    }

    // Сеттер для id
    public void setId(Long id) {
        this.id = id;
    }

    // Геттер для code
    public String getCode() {
        return code;
    }

    // Сеттер для code
    public void setCode(String code) {
        this.code = code;
    }

    // Геттер для name
    public String getName() {
        return name;
    }

    // Сеттер для name
    public void setName(String name) {
        this.name = name;
    }
}

