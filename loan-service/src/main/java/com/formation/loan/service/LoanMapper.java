package com.formation.loan.service;

import com.formation.loan.dto.LoanRequest;
import com.formation.loan.dto.LoanResponse;
import com.formation.loan.model.Loan;
import com.formation.loan.model.LoanStatus;

import java.time.LocalDate;

public final class LoanMapper {

    private LoanMapper() {
    }

    public static Loan toEntity(LoanRequest request, String bookTitle) {
        LocalDate loanDate = LocalDate.now();
        return new Loan(
                request.getMemberName(),
                request.getBookId(),
                bookTitle,
                loanDate,
                loanDate.plusDays(14),
                LoanStatus.ACTIVE
        );
    }

    public static LoanResponse toResponse(Loan loan) {
        return new LoanResponse(
                loan.getId(),
                loan.getMemberName(),
                loan.getBookId(),
                loan.getBookTitle(),
                loan.getLoanDate(),
                loan.getDueDate(),
                loan.getReturnDate(),
                loan.getStatus()
        );
    }
}
