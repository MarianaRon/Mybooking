package com.example.mybooking.service;

import com.example.mybooking.model.SearchHotel;
import com.example.mybooking.repository.ISearchHotelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SearchHotelService {

    @Autowired
    private ISearchHotelRepository searchHotelRepository;

    public void saveSearch(SearchHotel searchHotel) {
        searchHotelRepository.save(searchHotel);
    }
}
