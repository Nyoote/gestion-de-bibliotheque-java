package com.formation.book.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.formation.book.dto.BookRequest;
import com.formation.book.dto.BookResponse;
import com.formation.book.exception.BookNotFoundException;
import com.formation.book.exception.StockLimitExceededException;
import com.formation.book.exception.StockUnavailableException;
import com.formation.book.model.Book;
import com.formation.book.repository.BookRepository;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookService bookService;

    @Test
    void decrementStockSurUnLivreEpuiseLeveUneException() {
        Book book = book(1L, 3, 0);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        assertThatThrownBy(() -> bookService.decrementStock(1L))
                .isInstanceOf(StockUnavailableException.class);
        verify(bookRepository, never()).save(any());
    }

    @Test
    void incrementStockAuDelaDuTotalLeveUneException() {
        Book book = book(1L, 3, 3);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        assertThatThrownBy(() -> bookService.incrementStock(1L))
                .isInstanceOf(StockLimitExceededException.class);
        verify(bookRepository, never()).save(any());
    }

    @Test
    void findByIdLivreInexistantLeveUneException() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.findById(99L))
                .isInstanceOf(BookNotFoundException.class);
    }

    @Test
    void createInitialiseLeStockDisponibleAuTotal() {
        BookRequest request = request("Titre", "Auteur", "ISBN", 4, 0);
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookResponse response = bookService.create(request);

        assertThat(response.getTotalCopies()).isEqualTo(4);
        assertThat(response.getAvailableCopies()).isEqualTo(4);
    }

    @Test
    void findAllRetourneLesLivres() {
        when(bookRepository.findAll()).thenReturn(List.of(book(1L, 2, 1)));

        assertThat(bookService.findAll())
                .extracting(BookResponse::getId)
                .containsExactly(1L);
    }

    @Test
    void updateModifieUnLivre() {
        Book existing = book(1L, 2, 1);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(bookRepository.save(existing)).thenReturn(existing);

        BookResponse response = bookService.update(1L, request("Nouveau", "Auteur 2", "ISBN-2", 5, 3));

        assertThat(response.getTitle()).isEqualTo("Nouveau");
        assertThat(response.getAvailableCopies()).isEqualTo(3);
        verify(bookRepository).save(existing);
    }

    @Test
    void deleteSupprimeLeLivre() {
        Book book = book(1L, 2, 1);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        bookService.delete(1L);

        verify(bookRepository).delete(book);
    }

    private Book book(Long id, int totalCopies, int availableCopies) {
        return new Book(id, "Titre", "Auteur", "ISBN-" + id, totalCopies, availableCopies);
    }

    private BookRequest request(String title, String author, String isbn, int totalCopies, int availableCopies) {
        BookRequest request = new BookRequest();
        request.setTitle(title);
        request.setAuthor(author);
        request.setIsbn(isbn);
        request.setTotalCopies(totalCopies);
        request.setAvailableCopies(availableCopies);
        return request;
    }
}