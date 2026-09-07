package com.formation.book.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class StockLimitExceededException extends RuntimeException {

    public StockLimitExceededException(Long id) {
        super("Le stock disponible ne peut pas depasser le stock total pour le livre : " + id);
    }
}