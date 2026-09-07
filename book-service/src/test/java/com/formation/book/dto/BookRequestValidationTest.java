package com.formation.book;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import com.formation.book.dto.BookRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.Test;

class BookRequestValidationTest {

    private final Validator validator;

    BookRequestValidationTest() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void accepteUneRequeteValide() {
        BookRequest request = request("Titre", "Auteur", "ISBN", 3, 2);

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void rejetteLesChampsObligatoires() {
        BookRequest request = request("", " ", "", null, 0);

        Set<ConstraintViolation<BookRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(ConstraintViolation::getPropertyPath)
                .extracting(Object::toString)
                .contains("title", "author", "isbn", "totalCopies");
    }

    @Test
    void rejetteUnNombreTotalDeCopiesInferieurAUn() {
        BookRequest request = request("Titre", "Auteur", "ISBN", 0, 0);

        assertThat(validator.validate(request))
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("totalCopies"));
    }

    private BookRequest request(
            String title,
            String author,
            String isbn,
            Integer totalCopies,
            Integer availableCopies) {

        BookRequest request = new BookRequest();
        request.setTitle(title);
        request.setAuthor(author);
        request.setIsbn(isbn);
        request.setTotalCopies(totalCopies);
        request.setAvailableCopies(availableCopies);
        return request;
    }
}