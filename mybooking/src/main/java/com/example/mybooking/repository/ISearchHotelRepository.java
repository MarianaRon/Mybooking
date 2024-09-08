package com.example.mybooking.repository;
import com.example.mybooking.model.SearchHotel;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ISearchHotelRepository extends JpaRepository<SearchHotel, Long> {
}
