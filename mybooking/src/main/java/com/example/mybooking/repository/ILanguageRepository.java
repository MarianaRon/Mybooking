package com.example.mybooking.repository;


import com.example.mybooking.model.Language;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Репозиторий для работы с сущностью Language.
 */
@Repository
public interface ILanguageRepository extends JpaRepository<Language, Long> {
}