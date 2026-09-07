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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class LoanService {

    private final LoanRepository loanRepository;
    private final BookClient bookClient;

    public LoanService(LoanRepository loanRepository, BookClient bookClient) {
        this.loanRepository = loanRepository;
        this.bookClient = bookClient;
    }

    @Transactional(readOnly = true)
    public List<LoanResponse> findAll() {
        return loanRepository.findAll().stream()
                .map(LoanMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public LoanResponse findById(Long id) {
        return LoanMapper.toResponse(getLoanOrThrow(id));
    }

    @Transactional
    public LoanResponse create(LoanRequest request) {
        BookDto book = fetchBook(request.getBookId());

        if (book.getAvailableCopies() == null || book.getAvailableCopies() <= 0) {
            throw new NoAvailableCopyException(request.getBookId());
        }

        decrementStock(request.getBookId());

        Loan loan = LoanMapper.toEntity(request, book.getTitle());
        return LoanMapper.toResponse(loanRepository.save(loan));
    }

    @Transactional
    public LoanResponse returnLoan(Long id) {
        Loan loan = getLoanOrThrow(id);

        if (loan.getStatus() == LoanStatus.RETURNED) {
            throw new LoanAlreadyReturnedException(id);
        }

        incrementStock(loan.getBookId());

        loan.setStatus(LoanStatus.RETURNED);
        loan.setReturnDate(LocalDate.now());
        return LoanMapper.toResponse(loanRepository.save(loan));
    }

    private BookDto fetchBook(Long bookId) {
        try {
            return bookClient.getBookById(bookId);
        } catch (FeignException.NotFound ex) {
            throw new BookNotFoundForLoanException(bookId);
        } catch (FeignException ex) {
            throw new BookServiceUnavailableException(ex.getMessage());
        }
    }

    private void decrementStock(Long bookId) {
        try {
            bookClient.decrementStock(bookId);
        } catch (FeignException.Conflict ex) {
            throw new NoAvailableCopyException(bookId);
        } catch (FeignException.NotFound ex) {
            throw new BookNotFoundForLoanException(bookId);
        } catch (FeignException ex) {
            throw new BookServiceUnavailableException(ex.getMessage());
        }
    }

    private void incrementStock(Long bookId) {
        try {
            bookClient.incrementStock(bookId);
        } catch (FeignException.NotFound ex) {
            throw new BookNotFoundForLoanException(bookId);
        } catch (FeignException ex) {
            throw new BookServiceUnavailableException(ex.getMessage());
        }
    }

    private Loan getLoanOrThrow(Long id) {
        return loanRepository.findById(id)
                .orElseThrow(() -> new LoanNotFoundException(id));
    }
}
