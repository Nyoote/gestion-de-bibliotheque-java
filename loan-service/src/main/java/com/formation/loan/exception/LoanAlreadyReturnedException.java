package com.formation.loan.exception;

public class LoanAlreadyReturnedException extends RuntimeException {

    public LoanAlreadyReturnedException(Long id) {
        super("Cet emprunt est deja termine (id " + id + ")");
    }
}
