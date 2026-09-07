package com.formation.book.service;

import com.formation.book.dto.BookRequest;
import com.formation.book.dto.BookResponse;
import com.formation.book.model.Book;

public final class BookMapper {

    private BookMapper() {
    }

    public static Book toEntity(BookRequest request) {
        return new Book(request.getTitle(), request.getAuthor(), request.getIsbn(), request.getTotalCopies(),
                request.getAvailableCopies());
    }

    public static BookResponse toResponse(Book book) {
        return new BookResponse(book.getId(), book.getTitle(), book.getAuthor(),
                book.getIsbn(), book.getTotalCopies(), book.getAvailableCopies());
    }
}