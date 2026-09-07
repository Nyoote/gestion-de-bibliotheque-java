package com.formation.book.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.formation.book.dto.BookRequest;
import com.formation.book.dto.BookResponse;
import com.formation.book.exception.BookNotFoundException;
import com.formation.book.exception.StockLimitExceededException;
import com.formation.book.exception.StockUnavailableException;
import com.formation.book.model.Book;
import com.formation.book.repository.BookRepository;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Transactional(readOnly = true)
    public List<BookResponse> findAll() {
        return bookRepository.findAll().stream()
                .map(BookMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public BookResponse findById(Long id) {
        return BookMapper.toResponse(getBookOrThrow(id));
    }

    @Transactional
    public BookResponse create(BookRequest request) {
        Book book = new Book(request.getTitle(), request.getAuthor(), request.getIsbn(),
                request.getTotalCopies(), request.getTotalCopies());
        return BookMapper.toResponse(bookRepository.save(book));
    }

    @Transactional
    public BookResponse update(Long id, BookRequest request) {
        Book book = getBookOrThrow(id);
        validateStock(request.getTotalCopies(), request.getAvailableCopies());
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setIsbn(request.getIsbn());
        book.setTotalCopies(request.getTotalCopies());
        book.setAvailableCopies(request.getAvailableCopies());
        return BookMapper.toResponse(bookRepository.save(book));
    }

    @Transactional
    public void delete(Long id) {
        Book book = getBookOrThrow(id);
        bookRepository.delete(book);
    }

    @Transactional
    public BookResponse decrementStock(Long id) {
        Book book = getBookOrThrow(id);
        if (book.getAvailableCopies() <= 0) {
            throw new StockUnavailableException(id);
        }
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        return BookMapper.toResponse(bookRepository.save(book));
    }

    @Transactional
    public BookResponse incrementStock(Long id) {
        Book book = getBookOrThrow(id);
        if (book.getAvailableCopies() >= book.getTotalCopies()) {
            throw new StockLimitExceededException(id);
        }
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        return BookMapper.toResponse(bookRepository.save(book));
    }

    private Book getBookOrThrow(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));
    }

    private void validateStock(Integer totalCopies, Integer availableCopies) {
        if (availableCopies < 0 || availableCopies > totalCopies) {
            throw new IllegalArgumentException("Le stock disponible doit etre compris entre 0 et le total");
        }
    }
}