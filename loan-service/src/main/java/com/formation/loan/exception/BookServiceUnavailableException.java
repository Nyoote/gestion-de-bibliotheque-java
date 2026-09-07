package com.formation.loan.exception;

public class BookServiceUnavailableException extends RuntimeException {

    public BookServiceUnavailableException(String message) {
        super("book-service est indisponible, impossible de traiter la demande : " + message);
    }
}
