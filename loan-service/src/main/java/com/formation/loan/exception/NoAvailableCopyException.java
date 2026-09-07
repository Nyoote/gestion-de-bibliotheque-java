package com.formation.loan.exception;

public class NoAvailableCopyException extends RuntimeException {

    public NoAvailableCopyException(Long bookId) {
        super("Aucun exemplaire disponible pour ce livre (id " + bookId + ")");
    }
}
