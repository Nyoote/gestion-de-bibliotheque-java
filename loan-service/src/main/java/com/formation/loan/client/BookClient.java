package com.formation.loan.client;

import com.formation.loan.dto.BookDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "book-service")
public interface BookClient {

    @GetMapping("/api/books/{id}")
    BookDto getBookById(@PathVariable("id") Long id);

    @RequestMapping(method = RequestMethod.PATCH, value = "/api/books/{id}/decrement-stock")
    void decrementStock(@PathVariable("id") Long id);

    @RequestMapping(method = RequestMethod.PATCH, value = "/api/books/{id}/increment-stock")
    void incrementStock(@PathVariable("id") Long id);
}
