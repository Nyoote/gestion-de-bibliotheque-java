package com.formation.loan.service;

import com.formation.loan.client.BookClient;
import com.formation.loan.dto.BookDto;
import com.formation.loan.dto.LoanRequest;
import com.formation.loan.dto.LoanResponse;
import com.formation.loan.exception.BookNotFoundForLoanException;
import com.formation.loan.exception.BookServiceUnavailableException;
import com.formation.loan.exception.LoanAlreadyReturnedException;
import com.formation.loan.exception.LoanNotFoundException;
import com.formation.loan.exception.NoAvailableCopyException;
import com.formation.loan.model.Loan;
import com.formation.loan.model.LoanStatus;
import com.formation.loan.repository.LoanRepository;
import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private BookClient bookClient;

    @InjectMocks
    private LoanService loanService;

    @Test
    void create_livreInexistant_leveBookNotFoundForLoan() {
        when(bookClient.getBookById(99L)).thenThrow(notFoundException());

        LoanRequest request = new LoanRequest("Bob", 99L);

        assertThatThrownBy(() -> loanService.create(request))
                .isInstanceOf(BookNotFoundForLoanException.class)
                .hasMessageContaining("99");
    }

    @Test
    void create_stockEpuise_leveNoAvailableCopyEtNappellePasDecrement() {
        BookDto book = new BookDto(3L, "Le Petit Prince", "Saint Exupery", "123456", 1, 0);
        when(bookClient.getBookById(3L)).thenReturn(book);

        LoanRequest request = new LoanRequest("Bob", 3L);

        assertThatThrownBy(() -> loanService.create(request))
                .isInstanceOf(NoAvailableCopyException.class)
                .hasMessageContaining("3");

        verify(bookClient, never()).decrementStock(3L);
        verify(loanRepository, never()).save(any());
    }

    @Test
    void create_casDeConcurrence_bookServiceRepond409_leveNoAvailableCopy() {
        BookDto book = new BookDto(3L, "Le Petit Prince", "Saint Exupery", "123456", 1, 1);
        when(bookClient.getBookById(3L)).thenReturn(book);
        org.mockito.Mockito.doThrow(conflictException()).when(bookClient).decrementStock(3L);

        LoanRequest request = new LoanRequest("Bob", 3L);

        assertThatThrownBy(() -> loanService.create(request))
                .isInstanceOf(NoAvailableCopyException.class)
                .hasMessageContaining("3");

        verify(loanRepository, never()).save(any());
    }

    @Test
    void create_nominal_sauvegardeEmpruntActifAvecDueDatePlus14Jours() {
        BookDto book = new BookDto(3L, "Le Petit Prince", "Saint Exupery", "123456", 5, 5);
        when(bookClient.getBookById(3L)).thenReturn(book);

        LoanRequest request = new LoanRequest("Bob", 3L);

        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> {
            Loan loan = invocation.getArgument(0);
            loan.setId(1L);
            return loan;
        });

        LoanResponse result = loanService.create(request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getMemberName()).isEqualTo("Bob");
        assertThat(result.getBookId()).isEqualTo(3L);
        assertThat(result.getBookTitle()).isEqualTo("Le Petit Prince");
        assertThat(result.getStatus()).isEqualTo(LoanStatus.ACTIVE);
        assertThat(result.getReturnDate()).isNull();
        assertThat(result.getDueDate()).isEqualTo(result.getLoanDate().plusDays(14));

        verify(bookClient).decrementStock(3L);
    }

    @Test
    void findById_empruntInexistant_leveLoanNotFound() {
        when(loanRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loanService.findById(99L))
                .isInstanceOf(LoanNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void returnLoan_dejaRendu_leveLoanAlreadyReturned() {
        Loan loan = new Loan("Bob", 3L, "Le Petit Prince", LocalDate.now().minusDays(3),
                LocalDate.now().plusDays(11), LoanStatus.RETURNED);
        loan.setReturnDate(LocalDate.now().minusDays(1));
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));

        assertThatThrownBy(() -> loanService.returnLoan(1L))
                .isInstanceOf(LoanAlreadyReturnedException.class)
                .hasMessageContaining("1");

        verify(bookClient, never()).incrementStock(3L);
    }

    @Test
    void returnLoan_nominal_marqueRenduEtReincrementeLeStock() {
        Loan loan = new Loan("Bob", 3L, "Le Petit Prince", LocalDate.now().minusDays(3),
                LocalDate.now().plusDays(11), LoanStatus.ACTIVE);
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LoanResponse result = loanService.returnLoan(1L);

        assertThat(result.getStatus()).isEqualTo(LoanStatus.RETURNED);
        assertThat(result.getReturnDate()).isEqualTo(LocalDate.now());

        verify(bookClient).incrementStock(3L);
    }

    @Test
    void returnLoan_bookServiceIndisponible_leveBookServiceUnavailable() {
        Loan loan = new Loan("Bob", 3L, "Le Petit Prince", LocalDate.now().minusDays(3),
                LocalDate.now().plusDays(11), LoanStatus.ACTIVE);
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        org.mockito.Mockito.doThrow(new FeignException.InternalServerError(
                "boom", feign.Request.create(feign.Request.HttpMethod.PATCH,
                "http://book-service/api/books/3/increment-stock", java.util.Map.of(), null,
                feign.Util.UTF_8, null), new byte[0], java.util.Map.of())).when(bookClient).incrementStock(3L);

        assertThatThrownBy(() -> loanService.returnLoan(1L))
                .isInstanceOf(BookServiceUnavailableException.class);
    }

    private static FeignException.NotFound notFoundException() {
        feign.Request request = feign.Request.create(feign.Request.HttpMethod.GET,
                "http://book-service/api/books/99", java.util.Map.of(), null,
                feign.Util.UTF_8, null);
        feign.Response response = feign.Response.builder()
                .status(404)
                .reason("Not Found")
                .request(request)
                .body(new byte[0])
                .build();
        return (FeignException.NotFound) FeignException.errorStatus("GET /api/books/99", response);
    }

    private static FeignException.Conflict conflictException() {
        feign.Request request = feign.Request.create(feign.Request.HttpMethod.PATCH,
                "http://book-service/api/books/3/decrement-stock", java.util.Map.of(), null,
                feign.Util.UTF_8, null);
        feign.Response response = feign.Response.builder()
                .status(409)
                .reason("Conflict")
                .request(request)
                .body(new byte[0])
                .build();
        return (FeignException.Conflict) FeignException.errorStatus("PATCH /api/books/3/decrement-stock", response);
    }
}
