package com.formation.loan.exception;

public class BookNotFoundForLoanException extends RuntimeException {

    public BookNotFoundForLoanException(Long bookId) {
        super("Le livre " + bookId + " demande dans l'emprunt est introuvable");
    }
}
