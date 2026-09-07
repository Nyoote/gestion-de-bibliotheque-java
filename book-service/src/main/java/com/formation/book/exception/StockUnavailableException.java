package com.formation.book.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class StockUnavailableException extends RuntimeException {

    public StockUnavailableException(Long id) {
        super("Aucun exemplaire disponible pour le livre : " + id);
    }
}