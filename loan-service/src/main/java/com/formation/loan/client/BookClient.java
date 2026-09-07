package com.formation.loan.client;

import com.formation.loan.dto.BookDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "book-service")
public interface BookClient {

    @GetMapping("/api/books/{id}")
    BookDto getBookById(@PathVariable("id") Long id);

    @PatchMapping("/api/books/{id}/decrement-stock")
    void decrementStock(@PathVariable("id") Long id);

    @PatchMapping("/api/books/{id}/increment-stock")
    void incrementStock(@PathVariable("id") Long id);
}
